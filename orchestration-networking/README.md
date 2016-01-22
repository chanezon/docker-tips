# Container Orchestration using Docker tools: Engine, Swarm, Machine, Compose

This demo uses the latest networking features introduced in Docker 1.9 and shows how machine, compose and swarm work together to help you orchestrate containers. The core of the demo comes from @dave-tucker, you can find his scripts at https://github.com/dave-tucker/docker-network-demos. The application used in this demo is [spring-doge](https://github.com/joshlong/spring-doge) by @joshlong and @philwebb, a Spring Boot Java application using MongoDB and MongoFS.

I have shown it at JavaOne 2015, [video of the demo](https://www.youtube.com/watch?v=S9XP8S85XaI&t=6h10m03s), [slides](http://www.slideshare.net/chanezon/docker-orchestration-welcome-to-the-jungle-javaone-2015)

You need Docker 1.9.0, and corresponding latest versions of compose and machine, which you can install with [docker-toolbox 1.9.0](https://github.com/docker/toolbox/releases/tag/v1.9.0)
As of today you need:
* docker 1.9.0
* docker-machine 0.5.0
* docker-compose 1.5.0

To run it:
```
./swarm-local.sh
eval $(docker-machine env --swarm swl-demo0)
docker-compose -f with-networking.yml up -d
docker-compose -f with-networking.yml scale web=3
```
Then in another terminal:
```
open http://$(docker-machine ip swl-demo0):8080
```

```docker-compose.yml``` is the old style compose for the spring-doge app, using links. ```with-networking.yml``` is the new style compose file without links, leveraging [the new docker networking support in compose](https://github.com/docker/compose/blob/master/docs/networking.md). It uses:
* environment variable ```container_name``` to force the name of the mongo container to be db (else compose will call it ```orchestrationnetworking_mongo_1```). This will be the hostname that networking will use for that container.
* ```"label:storage=ssd"``` environment variable to ask Swarm to schedule the mongo container on a host labeled with ssd storage (in our example swl-demo1)
* environment variable ```"affinity:com.docker.compose.service!=db"``` to ask Swarm to schedule the web container on a host different than the one where the db is hosted.
* environment variable ```MONGODB_URI=mongodb://db:27017/test``` that is expected from the spring-doge application that serves our web tier to connect to the database container. Here we leverage the fact that networking will assign db as the hostname.

You start docker-compose with the ```--x-networking``` to enable docker-compose networking integration, and specify ```--x-network-driver=overlay``` to create an overlay network.

This example shows:
* using docker-machine to provision a docker engine. Use docker to launch consul.
* using docker-machine to provision a swarm cluster, using the previous consul instance for swarm discovery and networking config store.
* using docker machine ```--engine-label``` option to apply labels to some of our swarm cluster engines, that we can use for scheduling constraints.
* using docker-compose swarm and networking integrations to schedule a Spring Boot app using Mongodb on a Swarm cluster.

New: using rexray for crossw cluster portable volumes.
```
eval $(docker-machine env --swarm swl-demo0)
docker-compose --x-networking --x-network-driver=overlay -f with-networking-and-volumes.yml up
```

# Orchestration

query label "com.docker.compose.service": "web",
"Networks": {
           "orchestrationnetworking": {
               "EndpointID": "94b9796ea65bd0191ceda076bd0d6ecdacdb54950a320f9bda404045c52cc556",
               "Gateway": "",
               "IPAddress": "10.0.2.6",
listen to events for stop, add
inspect
update haprocy
add yelp, deis
add yodocker

https://github.com/skippbox/k8sonpi

