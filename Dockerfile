FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY optilink-core optilink-core
COPY optilink-protocol optilink-protocol
COPY optilink-security optilink-security
COPY optilink-session optilink-session
COPY optilink-congestion optilink-congestion
COPY optilink-transport optilink-transport
COPY optilink-optical optilink-optical
COPY optilink-encoder optilink-encoder
COPY optilink-decoder optilink-decoder
COPY optilink-camera optilink-camera
COPY optilink-compression optilink-compression
COPY optilink-transfer optilink-transfer
COPY optilink-api optilink-api
COPY optilink-desktop optilink-desktop
COPY optilink-android optilink-android
COPY optilink-server optilink-server
COPY optilink-cli optilink-cli
COPY optilink-benchmark optilink-benchmark
COPY optilink-tests optilink-tests
COPY optilink-documentation optilink-documentation

RUN mvn -pl optilink-server -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV PORT=8080
ENV OPTILINK_TRANSFER_ROOT=/data/optilink-product-transfers
ENV OPTILINK_MAX_PAYLOAD_BYTES=131072

RUN mkdir -p /data/optilink-product-transfers
COPY --from=build /workspace/optilink-server/target/optilink-server-0.1.0-SNAPSHOT.jar /app/optilink-server.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar /app/optilink-server.jar --server.port=${PORT} --optilink.transfer.root=${OPTILINK_TRANSFER_ROOT} --optilink.transfer.max-payload-bytes=${OPTILINK_MAX_PAYLOAD_BYTES}"]
