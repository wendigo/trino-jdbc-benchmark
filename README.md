# Build

```
./mvnw clean install -nsu -T 2C -DskipTests
```

# Run either real deployment or Trino Product Test Environment with spooling

```
cd trino
./testing/bin/ptl env up --environment multinode-postgresql-spooling
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
./target/trino-jdbc-benchmark-1-SNAPSHOT-executable.jar "http://localhost:4317" "jdbc:trino://localhost:8080/tpch/sf10?encoding=json+zstd" "SELECT * FROM lineitem LIMIT 8000000" 8000000
./target/trino-jdbc-benchmark-1-SNAPSHOT-executable.jar "http://localhost:4317" "jdbc:trino://localhost:8080/tpch/sf10?encoding=json+lz4" "SELECT * FROM lineitem LIMIT 8000000" 8000000
```

# Or without (existing protocol)

```
./target/trino-jdbc-benchmark-1-SNAPSHOT-executable.jar "http://localhost:4317" "jdbc:trino://localhost:8080/tpch/sf10" "SELECT * FROM lineitem LIMIT 8000000" 8000000
```

# Check traces

[http://localhost:16686](http://localhost:16686)

# JVM debugging

Debugger is exposed on port :2137

# Some results

## Non-spooled JSON

```
Listening for transport dt_socket at address: 2137
Trino JDBC benchmark started with 10 runs for query 'SELECT * FROM lineitem LIMIT 4000000' for Trino URI: jdbc:trino://localhost:8080/tpch/sf10
=================================================
[QUERY 1] Time to first row: 536ms
[HEAP 1] Used before: 20.2 MB
[QUERY 1] Fetched 3999999 rows in 29260ms, decoding took 4977ms
[HEAP 1] Used after: 262.8 MB
=================================================
[QUERY 2] Time to first row: 147ms
[HEAP 2] Used before: 275.8 MB
[QUERY 2] Fetched 3999999 rows in 26963ms, decoding took 4598ms
[HEAP 2] Used after: 324.9 MB
=================================================
[QUERY 3] Time to first row: 149ms
[HEAP 3] Used before: 342.9 MB
[QUERY 3] Fetched 3999999 rows in 27111ms, decoding took 4674ms
[HEAP 3] Used after: 51.3 MB
=================================================
[QUERY 4] Time to first row: 170ms
[HEAP 4] Used before: 70.3 MB
[QUERY 4] Fetched 3999999 rows in 26637ms, decoding took 4586ms
[HEAP 4] Used after: 96.2 MB
=================================================
[QUERY 5] Time to first row: 121ms
[HEAP 5] Used before: 110.2 MB
[QUERY 5] Fetched 3999999 rows in 27638ms, decoding took 4713ms
[HEAP 5] Used after: 144.6 MB
=================================================
[QUERY 6] Time to first row: 153ms
[HEAP 6] Used before: 162.6 MB
[QUERY 6] Fetched 3999999 rows in 28200ms, decoding took 4885ms
[HEAP 6] Used after: 193.9 MB
=================================================
[QUERY 7] Time to first row: 134ms
[HEAP 7] Used before: 207.9 MB
[QUERY 7] Fetched 3999999 rows in 28780ms, decoding took 4950ms
[HEAP 7] Used after: 257.4 MB
=================================================
[QUERY 8] Time to first row: 121ms
[HEAP 8] Used before: 271.4 MB
[QUERY 8] Fetched 3999999 rows in 28013ms, decoding took 4755ms
[HEAP 8] Used after: 327.8 MB
=================================================
[QUERY 9] Time to first row: 128ms
[HEAP 9] Used before: 342.8 MB
[QUERY 9] Fetched 3999999 rows in 27821ms, decoding took 4797ms
[HEAP 9] Used after: 68.4 MB
=================================================
[QUERY 10] Time to first row: 128ms
[HEAP 10] Used before: 82.4 MB
[QUERY 10] Fetched 3999999 rows in 27411ms, decoding took 4789ms
[HEAP 10] Used after: 134.3 MB
Average time for 10 runs: 27783ms
```

## Spooled json

```
Listening for transport dt_socket at address: 2137
Trino JDBC benchmark started with 10 runs for query 'SELECT * FROM lineitem LIMIT 4000000' for Trino URI: jdbc:trino://localhost:8080/tpch/sf10?encoding=json
=================================================
[QUERY 1] Time to first row: 1047ms
[HEAP 1] Used before: 68.6 MB
[QUERY 1] Fetched 3999999 rows in 13734ms, decoding took 7464ms
[HEAP 1] Used after: 1.2 GB
=================================================
[QUERY 2] Time to first row: 784ms
[HEAP 2] Used before: 1.2 GB
[QUERY 2] Fetched 3999999 rows in 13378ms, decoding took 7347ms
[HEAP 2] Used after: 1.1 GB
=================================================
[QUERY 3] Time to first row: 670ms
[HEAP 3] Used before: 891.3 MB
[QUERY 3] Fetched 3999999 rows in 12739ms, decoding took 7256ms
[HEAP 3] Used after: 820.0 MB
=================================================
[QUERY 4] Time to first row: 631ms
[HEAP 4] Used before: 904.0 MB
[QUERY 4] Fetched 3999999 rows in 13195ms, decoding took 7431ms
[HEAP 4] Used after: 991.9 MB
=================================================
[QUERY 5] Time to first row: 581ms
[HEAP 5] Used before: 1.0 GB
[QUERY 5] Fetched 3999999 rows in 13037ms, decoding took 7428ms
[HEAP 5] Used after: 947.4 MB
=================================================
[QUERY 6] Time to first row: 658ms
[HEAP 6] Used before: 1.0 GB
[QUERY 6] Fetched 3999999 rows in 12918ms, decoding took 7318ms
[HEAP 6] Used after: 1007.0 MB
=================================================
[QUERY 7] Time to first row: 632ms
[HEAP 7] Used before: 780.0 MB
[QUERY 7] Fetched 3999999 rows in 13215ms, decoding took 7442ms
[HEAP 7] Used after: 805.0 MB
=================================================
[QUERY 8] Time to first row: 632ms
[HEAP 8] Used before: 889.0 MB
[QUERY 8] Fetched 3999999 rows in 13401ms, decoding took 7542ms
[HEAP 8] Used after: 974.6 MB
=================================================
[QUERY 9] Time to first row: 641ms
[HEAP 9] Used before: 1.0 GB
[QUERY 9] Fetched 3999999 rows in 12636ms, decoding took 7322ms
[HEAP 9] Used after: 488.0 MB
=================================================
[QUERY 10] Time to first row: 606ms
[HEAP 10] Used before: 572.0 MB
[QUERY 10] Fetched 3999999 rows in 13260ms, decoding took 7397ms
[HEAP 10] Used after: 1.0 GB
Average time for 10 runs: 13151ms
```

## Spooled json+lz4

```
Listening for transport dt_socket at address: 2137
Trino JDBC benchmark started with 10 runs for query 'SELECT * FROM lineitem LIMIT 4000000' for Trino URI: jdbc:trino://localhost:8080/tpch/sf10?encoding=json+lz4
=================================================
[QUERY 1] Time to first row: 1055ms
[HEAP 1] Used before: 87.9 MB
[QUERY 1] Fetched 3999999 rows in 14355ms, decoding took 7460ms
[HEAP 1] Used after: 851.0 MB
=================================================
[QUERY 2] Time to first row: 674ms
[HEAP 2] Used before: 164.9 MB
[QUERY 2] Fetched 3999999 rows in 14868ms, decoding took 8217ms
[HEAP 2] Used after: 1.2 GB
=================================================
[QUERY 3] Time to first row: 640ms
[HEAP 3] Used before: 168.5 MB
[QUERY 3] Fetched 3999999 rows in 13240ms, decoding took 7263ms
[HEAP 3] Used after: 883.8 MB
=================================================
[QUERY 4] Time to first row: 663ms
[HEAP 4] Used before: 715.0 MB
[QUERY 4] Fetched 3999999 rows in 13285ms, decoding took 7313ms
[HEAP 4] Used after: 1.1 GB
=================================================
[QUERY 5] Time to first row: 924ms
[HEAP 5] Used before: 1.2 GB
[QUERY 5] Fetched 3999999 rows in 13069ms, decoding took 7201ms
[HEAP 5] Used after: 742.0 MB
=================================================
[QUERY 6] Time to first row: 599ms
[HEAP 6] Used before: 844.0 MB
[QUERY 6] Fetched 3999999 rows in 13394ms, decoding took 7490ms
[HEAP 6] Used after: 464.0 MB
=================================================
[QUERY 7] Time to first row: 771ms
[HEAP 7] Used before: 567.0 MB
[QUERY 7] Fetched 3999999 rows in 13533ms, decoding took 7466ms
[HEAP 7] Used after: 1.1 GB
=================================================
[QUERY 8] Time to first row: 630ms
[HEAP 8] Used before: 153.6 MB
[QUERY 8] Fetched 3999999 rows in 13364ms, decoding took 7325ms
[HEAP 8] Used after: 297.0 MB
=================================================
[QUERY 9] Time to first row: 626ms
[HEAP 9] Used before: 400.0 MB
[QUERY 9] Fetched 3999999 rows in 12482ms, decoding took 7376ms
[HEAP 9] Used after: 852.8 MB
=================================================
[QUERY 10] Time to first row: 597ms
[HEAP 10] Used before: 954.8 MB
[QUERY 10] Fetched 3999999 rows in 13345ms, decoding took 7342ms
[HEAP 10] Used after: 836.8 MB
Average time for 10 runs: 13493ms
```

## Spooled json+zstd

```
Trino JDBC benchmark started with 10 runs for query 'SELECT * FROM lineitem LIMIT 4000000' for Trino URI: jdbc:trino://localhost:8080/tpch/sf10?encoding=json+zstd
=================================================
[QUERY 1] Time to first row: 1221ms
[HEAP 1] Used before: 83.8 MB
[QUERY 1] Fetched 3999999 rows in 14776ms, decoding took 7273ms
[HEAP 1] Used after: 531.2 MB
=================================================
[QUERY 2] Time to first row: 561ms
[HEAP 2] Used before: 218.1 MB
[QUERY 2] Fetched 3999999 rows in 15130ms, decoding took 7786ms
[HEAP 2] Used after: 886.5 MB
=================================================
[QUERY 3] Time to first row: 670ms
[HEAP 3] Used before: 978.5 MB
[QUERY 3] Fetched 3999999 rows in 14085ms, decoding took 7340ms
[HEAP 3] Used after: 881.0 MB
=================================================
[QUERY 4] Time to first row: 623ms
[HEAP 4] Used before: 971.4 MB
[QUERY 4] Fetched 3999999 rows in 13944ms, decoding took 7291ms
[HEAP 4] Used after: 1.2 GB
=================================================
[QUERY 5] Time to first row: 642ms
[HEAP 5] Used before: 1.2 GB
[QUERY 5] Fetched 3999999 rows in 13777ms, decoding took 7333ms
[HEAP 5] Used after: 818.7 MB
=================================================
[QUERY 6] Time to first row: 619ms
[HEAP 6] Used before: 910.7 MB
[QUERY 6] Fetched 3999999 rows in 14069ms, decoding took 7333ms
[HEAP 6] Used after: 291.0 MB
=================================================
[QUERY 7] Time to first row: 667ms
[HEAP 7] Used before: 173.5 MB
[QUERY 7] Fetched 3999999 rows in 14074ms, decoding took 7430ms
[HEAP 7] Used after: 1018.0 MB
=================================================
[QUERY 8] Time to first row: 655ms
[HEAP 8] Used before: 1.1 GB
[QUERY 8] Fetched 3999999 rows in 13743ms, decoding took 7243ms
[HEAP 8] Used after: 890.7 MB
=================================================
[QUERY 9] Time to first row: 643ms
[HEAP 9] Used before: 274.3 MB
[QUERY 9] Fetched 3999999 rows in 15964ms, decoding took 8391ms
[HEAP 9] Used after: 951.0 MB
=================================================
[QUERY 10] Time to first row: 763ms
[HEAP 10] Used before: 152.3 MB
[QUERY 10] Fetched 3999999 rows in 14150ms, decoding took 7346ms
[HEAP 10] Used after: 775.7 MB
Average time for 10 runs: 14371ms
```
