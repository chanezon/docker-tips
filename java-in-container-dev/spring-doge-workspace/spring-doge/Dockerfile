FROM maven:3-jdk-8

MAINTAINER Patrick Chanezon <patrick@chanezon.com>

EXPOSE 8080
WORKDIR /usr/src/spring-doge
COPY . /usr/src/spring-doge
RUN mvn install && cp spring-doge/target/*.jar spring-doge.jar
CMD java -Dserver.port=8080 -Dspring.data.mongodb.uri=$MONGODB_URI -jar spring-doge.jar
