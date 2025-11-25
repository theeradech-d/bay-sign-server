## Install

```shell
mvn install
```

## Run

```shell
mvn clean package && java -jar target/sprbaysign-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Run with Docker Compose

```shell
docker-compose up -d
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

---

## Build and Push Docker Image

```shell
docker buildx build --platform linux/amd64,linux/arm64 -t theeradechd/bay-sign-server:latest .
docker push theeradechd/bay-sign-server:latest
```
