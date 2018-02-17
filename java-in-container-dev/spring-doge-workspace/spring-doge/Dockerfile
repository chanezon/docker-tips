FROM maven:3.5-jdk-8 as builder

MAINTAINER Patrick Chanezon <patrick@chanezon.com>

COPY . /usr/src
WORKDIR /usr/src
RUN mvn package

FROM openjdk:8u131-jre

EXPOSE 8080

COPY --from=builder /usr/src/spring-doge/target/*.jar /usr/app/spring-doge.jar
WORKDIR /usr/app
CMD java -Dserver.port=8080 -Dspring.data.mongodb.uri=$MONGODB_URI -jar spring-doge.jar
HEALTHCHECK --interval=5m --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1
