# Spring Doge - Such Boot!

```
cf push -p target/demo-0.0.1-SNAPSHOT.jar  -b  https://github.com/cloudfoundry/java-buildpack.git  doge

http://www.java-allandsundry.com/2014/08/deploying-spring-boot-application-to.html
```

Interesting introduction [to deploying Spring Boot applications using Spring Cloud](http://www.java-allandsundry.com/2014/08/deploying-spring-boot-application-to.html)
https://gist.github.com/relaxdiego/7539911

https://github.com/pivotal-cf/java-8-buildpack/blob/master/docs/example-java_main.md

## Building and running the application using docker and docker-compose

The maven build for this project integrates with Docker using the [Spotify Maven plugin](https://github.com/spotify/docker-maven-plugin). You need to have Docker installed and configured. See [Installing Docker](https://docs.docker.com/installation/).

In order to build a docker image:  
```
mvn package
```

You can customize the Docker Hub user for your image (by default it will create chanezon/spring-doge) in spring-doge/pom.xml.

Then to run the app:  
```
docker-compose -f spring-doge/target/docker-compose.yml up
```

## Old Building and running with Docker

You can use STS or a locally installation of maven to build spring-doge. However, if you need to build that on a new machine where you don't have all your development environment setup, you can build it with the maven Docker container.

```
docker run -v ~/.m2:/root/.m2 -v "$PWD":/usr/src -w /usr/src maven:3-jdk-8 mvn install
```

This will create the spring-doge jar file in target.
Then, you can build a container for the app and run the app and a Mongodb database in containers with the following commands:

```
docker build -t foo/spring-doge .
docker-compose up
```

Change chanezon to your docker hub username (change it also in the docker-compose.yml file) if you want to push a modification to this image to your repository in Docker hub.
