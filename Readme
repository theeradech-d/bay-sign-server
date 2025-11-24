## Install

```shell
mvn install
```

## Run

```shell
mvn clean package && java -jar target/sprbaysign-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Test Sign Data

[POST] http://localhost:3002/sign

```shell
curl --location 'http://localhost:3002/sign' \
--header 'x-bay-sign-public-key: [PUBLIC_KEY]' \
--header 'Content-Type: application/json' \
--data '{
    "test":"test"
}'
```
