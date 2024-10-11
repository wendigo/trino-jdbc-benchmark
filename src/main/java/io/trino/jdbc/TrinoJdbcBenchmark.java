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

    private final String trinoUri;
    private final String query;
    private final int expectedRows;

    public static void main(String[] args)
            throws Exception
    {
        if (args.length == 0) {
            System.out.println("Usage ./target/trino-jdbc-benchmark-1-SNAPSHOT-executable.jar [TRINO URI] [TRINO QUERY] [EXPECTED ROWS]");
        }
        checkState(args.length == 3, "Expected 3 arguments, got %s", args.length);

        TrinoJdbcBenchmark benchmark = new TrinoJdbcBenchmark(args[0], args[1], Integer.parseInt(args[2]));
        benchmark.run();
    }

    public TrinoJdbcBenchmark(String trinoUri, String query, int expectedRows)
    {
        this.trinoUri = trinoUri;
        this.query = query;
        this.expectedRows = expectedRows;
    }

    public void run()
            throws Exception
    {
        try (TrinoDriver driver = new TrinoDriver()) {
            System.out.println("Trino JDBC benchmark started with %s runs for query '%s' for Trino URI: %s".formatted(RUNS, query, trinoUri));

            long totalTime = 0;

            long progressBase = expectedRows / 50;

            for (int i = 1; i <= RUNS; i++) {
                long rows = 1;
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

                            if (rows % progressBase == 0) {
                                System.out.print("=");
                            }
                        }
                    }

                    long elapsedTime = System.nanoTime() - start;
                    totalTime += elapsedTime;

                    System.out.print("\n");
                    System.out.println("[QUERY %d] Fetched %d rows in %s, decoding took %s".formatted(i, rows, formatNanos(elapsedTime), formatNanos(decodingTime)));
                    System.out.println("[HEAP %d] Used after: %s".formatted(i, formatSize(getUsedHeapMemory())));
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                if (rows != expectedRows) {
                    System.err.println("Expected %d rows but got %d".formatted(expectedRows, rows));
                }
            }
            System.out.println("Average time for %d runs: %s".formatted(RUNS, formatNanos(totalTime / RUNS)));
            System.exit(0);
        }
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
