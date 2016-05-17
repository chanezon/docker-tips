# Spring Doge - Such Boot!

This is a forked version of @joshlong & @philwebb's [spring-doge](https://github.com/joshlong/spring-doge) microservice demo application. I haven't changed the code, but focus on how to develop and deploy it with Docker.


## Development in a container with Spring Tool Suite

In order to 
```
git clone https://github.com/chanezon/docker-tips.git
cd java-in-container-dev/spring-doge-workspace/spring-doge
startx
```

Check out [Running X11 applications in Docker for Mac](https://github.com/chanezon/docker-tips/tree/master/x11) for the startx script.

```
docker-compose up
```

Will launch Eclipse and Mongodb. You can start running and debugging your app right away, check the result in your browser at localhost:8080.

<img src="/img/sts-compose.png"/>

## Building the image

Either build the jar file with one container and copy it in the final image, or use the all-in-one Dockerfile to build the image. The all-in-one build is slow since it cannot reuse a maven volume. I typically use it only for CI/CD, and use the maven container for day to day builds.

```
docker run -it --rm -v $PWD:/usr/src/spring-doge -v maven:/root/.m2 -w /usr/src/spring-doge maven:3.3-jdk-8 mvn package
docker build -t chanezon/spring-doge -f Dockerfile.dev .
```

or

```
docker build -t chanezon/spring-doge .
```

## Deployment on Swarm or UCP with Interlock

```
docker-compose -f docker-compose.prod.yml up -d
docker-compose -f docker-compose.prod.yml scale web=2
```

`ehazlett/docker-demo` is a great container to test Interlock, since it shows the id of teh cointainer that is deployed.
```
docker run -d -P \
--label interlock.hostname=test1 \
--label interlock.domain=chanezon.com \
ehazlett/docker-demo
```

Then setup your dns to the IP address where the load balancer is exposed.
```
spring-doge 10800 IN CNAME patagt20.westus.cloudapp.azure.com.
```

