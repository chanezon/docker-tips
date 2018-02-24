# Using Kubernetes in Docker for Mac

## Look at your Kubernetes configuration

```
cat /Users/pat/.kube/config
kubectl get nodes
```

## Install the Kubernetes Dashboard

[Kubernetes Dashboard documentation](https://github.com/kubernetes/dashboard#kubernetes-dashboard)
[Dashboard access control documentation](https://github.com/kubernetes/dashboard/wiki/Access-control#admin-privileges)

```
cd kube/dashboard
kubectl create -f dashboard-admin.yaml
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/master/src/deploy/recommended/kubernetes-dashboard.yaml
kubectl proxy
```

Browse to the [dashboard](http://localhost:8001/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy/).

## Install Helm

[Helm install docs](https://github.com/kubernetes/helm/blob/master/docs/quickstart.md)
[Install Helm on your laptop](https://github.com/kubernetes/helm/blob/master/docs/install.md)

```
cd kube/helm
brew install kubernetes-helm
kubectl create -f rbac-config.yaml
helm init
```

## Build a Spring Boot application with Docker

```
cd java-in-container-dev/spring-doge-workspace/spring-doge/
```

Look the Dockerfile for multi stage build. This is the file that would be used in a CI/CD pipeline.

```
docker build -t chanezon/spring-doge .
```

Or look at Makefile and Dockerfile.dev for manual build using a container: that way the maven cache is reused from one build to the other. You'd use that in development.

```
make jar
make image
```

## Debug a Spring Boot application in a containerized IDE

see [X11 Setup](../x11/README.md)

```
cd java-in-container-dev/spring-doge-workspace/spring-doge/
startx
docker-compose -f docker-compose.ide.yml up
```

start debugging in STS.

## Deploy a a Spring Boot application in Kubernetes with Docker

```
cd java-in-container-dev/spring-doge-workspace/spring-doge/
startx
docker stack deploy --compose-file docker-compose.yml doge
docker stack ps doge
kubectl get all
```
