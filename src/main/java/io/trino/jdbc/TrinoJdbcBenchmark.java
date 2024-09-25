/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.jdbc;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.ServiceAttributes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;

public class TrinoJdbcBenchmark
{
    private static final int RUNS = 10;

    private final String tracingUri;
    private final String trinoUri;
    private final String query;

    public static void main(String[] args)
            throws Exception
    {
        if (args.length == 0) {
            System.out.println("Usage ./target/trino-jdbc-benchmark-1-SNAPSHOT-executable.jar [TRACING COLLECTOR URI] [TRINO URI] [TRINO QUERY]");
        }
        checkState(args.length == 3, "Expected 3 arguments, got %s", args.length);

        TrinoJdbcBenchmark benchmark = new TrinoJdbcBenchmark(args[0], args[1], args[2]);
        benchmark.run();
    }

    public TrinoJdbcBenchmark(String tracingUri, String trinoUri, String query)
    {
        this.tracingUri = tracingUri;
        this.trinoUri = trinoUri;
        this.query = query;
    }

    public void run()
            throws Exception
    {
        AttributesBuilder attributes = Attributes.builder()
                .put(ServiceAttributes.SERVICE_NAME, "trino-jdbc-benchmark");

        Resource resource = Resource.getDefault().merge(Resource.create(attributes.build()));
        SpanExporter exporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(tracingUri)
                .build();

        BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(exporter).build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SpanProcessor.composite(spanProcessor))
                .setResource(resource)
                .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();

        TracingTrinoDriver driver = new TracingTrinoDriver(openTelemetry);

        System.out.println("Trino JDBC benchmark started with %s runs for query '%s' for Trino URI: %s".formatted(RUNS, query, trinoUri));

        long totalTime = 0;

        for (int i = 1; i <= RUNS; i++) {
            System.out.println("=================================================");
            long rows = 0;
            long start = System.nanoTime();
            long decodingTime = 0;
            try (Connection connection = driver.connect(trinoUri, connectionProperties())) {
                try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
                    resultSet.next();
                    decodingTime += readColumns(resultSet);
                    System.out.println("[QUERY %d] Time to first row: %s".formatted(i, formatNanos(System.nanoTime() - start)));
                    System.out.println("[HEAP %d] Used before: %s".formatted(i, formatSize(getUsedHeapMemory())));
                    while (resultSet.next()) {
                        rows++;
                        decodingTime += readColumns(resultSet);
                    }
                }

                long elapsedTime = System.nanoTime() - start;
                totalTime += elapsedTime;

                System.out.println("[QUERY %d] Fetched %d rows in %s, decoding took %s".formatted(i, rows, formatNanos(elapsedTime), formatNanos(decodingTime)));
                System.out.println("[HEAP %d] Used after: %s".formatted(i, formatSize(getUsedHeapMemory())));
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Average time for %d runs: %s".formatted(RUNS, formatNanos(totalTime / RUNS)));
        System.exit(0);
    }

    private static long readColumns(ResultSet resultSet)
            throws SQLException
    {
        long start = System.nanoTime();
        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
            resultSet.getObject(i); // Touch the column
        }
        return System.nanoTime() - start;
    }

    private static String formatNanos(long nanos)
    {
        return TimeUnit.NANOSECONDS.toMillis(nanos) + "ms";
    }

    private static Properties connectionProperties()
    {
        Properties properties = new Properties();
        properties.put("user", "trino");
        // properties.put("httpLoggingLevel", "BASIC");
        return properties;
    }

    private static long getUsedHeapMemory()
    {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public static String formatSize(long size)
    {
        if (size < 1024) {
            return size + " B";
        }
        int z = (63 - Long.numberOfLeadingZeros(size)) / 10;
        return String.format("%.1f %sB", (double) size / (1L << (z * 10)), " KMGTPE".charAt(z));
    }
}
