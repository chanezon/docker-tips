# Using a Volume for Maven, Gradle or sbt cache when compiling Java in a container

This is part of the [P@ Docker Tips](/README.md) series or tips about Docker.

Today's tip features is from me: during lunch I attended a very cool demo by one of my colleagues, featuring Scala code in a container. The demo was very cool, but dampened by the fact that after changing one line of Scala code, he rebuilt his Docker image and that took 15 minutes. Looking at sbt refetching all the jars from online maven repositories, I went to talk to him afterwards and told him: all this fetching is not necessary.

## The problem

If you are building Java code in a Docker image using Maven or Gradle, or Scala code using sbt, maven will look for all your code's dependencies in it's local cache $HOME/.m2 ($HOME/.sbt for Scala). If they are not there, it will fetch all dependencies from online Maven repositories. Since your Java code is built in the container, $HOME means the home directory of the user doing the build **in the container**. Except if you have pre-seeded a layer with a Maven cache, there is nothing in there, and Maven will fetch for all dependencies everytime. This problem and potential solutions are explained in [http://blog.flurdy.com/2014/11/dont-download-internet-share-maven-ivy-docker.html][Don't download the internet. Share Maven and Ivy repositories with Docker containers].

## The solution: mount a Volume for $HOME/.m2

The solution is quite easy: you need to mount a Volume for $HOME/.m2 ($HOME/.sbt for Scala). Unfortunately, you cannot mount a Volume when building an image. This means that if you want to compile your Java code in a container, before building an image, you need to use one container to compile, with a mounted volume for $HOME/.m2, and one for your source and target directory, then build an image with the resulting files.

[Here is an example for a Spring Boot application](https://github.com/chanezon/spring-doge) using this approach.

In order to compile your code with the [official Maven container](https://registry.hub.docker.com/_/maven/), cd to your source directory and run:
```
docker run -v ~/.m2:/root/.m2 -v "$PWD":/usr/src -w /usr/src maven:3-jdk-8 mvn package
```

The compilation artifacts will be in $PWD/target (on Mac OS X, docker-machine mounts /Users/yourusername in the Virtualbox VM).

Then you can have a Dockerfile like this to build the image for your app:
```
COPY spring-doge/target/*.jar /usr/src/spring-doge/spring-doge.jar
WORKDIR /usr/src/spring-doge
CMD java -Dserver.port=8080 -Dspring.data.mongodb.uri=$MONGODB_URI -jar spring-doge.jar
```

@cer is using the same approach for hi Gradle builds http://www.slideshare.net/chris.e.richardson/developing-and-deploying-spring-boot-applications-with-docker-oakjug/53
