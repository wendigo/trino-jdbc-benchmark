# Build

```
./mvnw clean install -nsu -T 2C -DskipTests
```
# Run Jaeger locally

```
docker run --rm --name jaeger \
  -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 14250:14250 \
  -p 14268:14268 \
  -p 14269:14269 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.61.0
```
# Run some queries with JSON encoding (spooled protocol)
```
./target/trino-jdbc-benchmark-1-SNAPSHOT-executable.jar "http://localhost:4317", "jdbc:trino://localhost:8080/tpch/sf10?encoding=json+zstd" "SELECT * FROM lineitem LIMIT 8000000"
./target/trino-jdbc-benchmark-1-SNAPSHOT-executable.jar "http://localhost:4317", "jdbc:trino://localhost:8080/tpch/sf10?encoding=json+lz4" "SELECT * FROM lineitem LIMIT 8000000"
```

# Or without (existing protocol)
```
./target/trino-jdbc-benchmark-1-SNAPSHOT-executable.jar "http://localhost:4317", "jdbc:trino://localhost:8080/tpch/sf10" "SELECT * FROM lineitem LIMIT 8000000"
```

# Check traces

[http://localhost:16686](http://localhost:16686)

# JVM debugging

Debugger is exposed on port :2137