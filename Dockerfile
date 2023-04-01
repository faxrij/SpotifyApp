#FROM registry.gitlab.com/research-ecosystems-apds/server/base:latest as builder

WORKDIR /usr/local/app

COPY src ./

RUN mvn \
    -Dmaven.test.skip=true \
    --batch-mode \
    package


FROM gcr.io/distroless/java:8

EXPOSE  8080
WORKDIR /usr/local/app

COPY --from=builder \
    /usr/local/app/target/spotify*.jar \
    ./server.jar

COPY ./src/main/resources ./

CMD [ "/usr/local/app/server.jar", "-Djava.security.edg=file:/dev/./urandom", "-Djava.security.egd=file:/dev/./urandom"]