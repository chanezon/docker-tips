FROM java:8

MAINTAINER Patrick Chanezon <patrick@chanezon.com>

EXPOSE 8080

WORKDIR /usr/src
ADD spring-doge.jar app.jar
CMD java -Dserver.port=8080 -Dspring.data.mongodb.uri=$MONGODB_URI -jar app.jar
