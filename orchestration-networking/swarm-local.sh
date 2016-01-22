#!/bin/bash

SWARM_IMAGE=swarm:1.1.0-rc1
BOOT2DOCKER_IMAGE="https://github.com/tianon/boot2docker-legacy/releases/download/v1.10.0-rc1/boot2docker.iso"

# Docker Machine Setup
docker-machine create \
	-d virtualbox \
    --virtualbox-boot2docker-url $BOOT2DOCKER_IMAGE \
	swl-consul

docker $(docker-machine config swl-consul) run -d --restart=always \
	-p "8500:8500" \
	-h "consul" \
	progrium/consul -server -bootstrap
	
docker-machine create \
	-d virtualbox \
    --virtualbox-boot2docker-url $BOOT2DOCKER_IMAGE \
	--swarm \
	--swarm-image="$SWARM_IMAGE" \
	--swarm-master \
	--swarm-discovery="consul://$(docker-machine ip swl-consul):8500" \
	--engine-opt="cluster-store=consul://$(docker-machine ip swl-consul):8500" \
    --engine-opt="cluster-advertise=eth1:0" \
	swl-demo0

docker-machine create \
	-d virtualbox \
    --virtualbox-boot2docker-url $BOOT2DOCKER_IMAGE \
	--swarm \
	--swarm-image="$SWARM_IMAGE" \
	--swarm-discovery="consul://$(docker-machine ip swl-consul):8500" \
	--engine-opt="cluster-store=consul://$(docker-machine ip swl-consul):8500" \
    --engine-opt="cluster-advertise=eth1:0" \
	--engine-label "storage=ssd" \
    swl-demo1

sleep 2

# Let's point at swarm
eval $(docker-machine env --swarm swl-demo0)

# Create an overlay network
docker network create -d overlay berlin

# Check that it's on both hosts
docker network ls

# Try it out!

docker run -itd --name=web --net=berlin --env="constraint:node==swl-demo0" nginx
docker run -it --rm --net=berlin --env="constraint:node==swl-demo1" busybox wget -O- http://web
