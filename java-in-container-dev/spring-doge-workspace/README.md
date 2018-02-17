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

Either build the jar file with one container and copy it in the final image, or use the all-in-one Dockerfile that uses multi-stage build to build the image. The all-in-one build is slow since it cannot reuse a maven volume. I typically use it only for CI/CD, and use the maven container for day to day builds.

```
docker run -it --rm -v $PWD:/usr/src/spring-doge -v maven:/root/.m2 -w /usr/src/spring-doge maven:3.3-jdk-8 mvn package
docker build -t chanezon/spring-doge -f Dockerfile.dev .
```

or

```
docker build -t chanezon/spring-doge .
```

## Development on Desktop using docker-compose

```
docker-compose up
```

## Deployment on Desktop using Kubernetes

```
docker stack deploy --compose-file docker-compose.yml spring-doge
docker stack ps spring-doge
```

When you do a `docker stack deploy`, the Kubernetes CRD controller translates the compose file into Kubernetes objects: deployments, replica sets, pods, services, all with the label `com.docker.stack.namespace` set to the name of the stack.

```
kubectl get all -l com.docker.stack.namespace=spring-doge
NAME           DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
deploy/mongo   1         1         1            1           4m
deploy/web     1         1         1            1           4m

NAME                  DESIRED   CURRENT   READY     AGE
rs/mongo-86b45bd97f   1         1         1         4m
rs/web-796c5fd447     1         1         1         4m

NAME                        READY     STATUS    RESTARTS   AGE
po/mongo-86b45bd97f-hvlx8   1/1       Running   0          4m
po/web-796c5fd447-sh45j     1/1       Running   0          4m

NAME                TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
svc/mongo           ClusterIP      None             <none>        55555/TCP        4m
svc/web             ClusterIP      None             <none>        55555/TCP        4m
svc/web-published   LoadBalancer   10.108.142.182   <pending>     8080:30908/TCP   4m
```

When you expose a port in a compose file, the stack controller creates a Kubernetes service, and a load balancer for it (Docker for Mac acts as the load balancer).

If you look at the list of services you might notice something that seems a bit odd at first glance. There are services for both web and web-published. The web service allows for intra-application communication, whereas the web-published service (which is a load balancer backed by vpnkit in Docker for Mac) exposes our web front end out to the rest of the world.

```
kubectl describe service web-published
Name:                     web-published
Namespace:                default
Labels:                   com.docker.service.id=spring-doge-web
                          com.docker.service.name=web
                          com.docker.stack.namespace=spring-doge
Annotations:              <none>
Selector:                 com.docker.service.id=spring-doge-web,com.docker.service.name=web,com.docker.stack.namespace=spring-doge
Type:                     LoadBalancer
IP:                       10.108.142.182
Port:                     8080  8080/TCP
TargetPort:               8080/TCP
NodePort:                 8080  30908/TCP
Endpoints:                10.1.1.58:8080
Session Affinity:         None
External Traffic Policy:  Cluster
Events:                   <none>
```

Browse to `localhost:8080` in a browser on your machine.

## Deployment on Docker EE using Kubernetes

```
docker stack deploy --compose-file docker-compose-prod.yml spring-doge
```

Or paste the docker-compose-prod.yml in the UI.


