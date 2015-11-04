#!/bin/bash

SWARM_IMAGE=swarm:1.0.0
# Docker Machine Setup
docker-machine create \
	-d virtualbox \
	swl-consul

docker $(docker-machine config swl-consul) run -d --restart=always \
	-p "8500:8500" \
	-h "consul" \
	progrium/consul -server -bootstrap
	
docker-machine create \
	-d virtualbox \
	--swarm \
	--swarm-image="$SWARM_IMAGE" \
	--swarm-master \
	--swarm-discovery="consul://$(docker-machine ip swl-consul):8500" \
	--engine-opt="cluster-store=consul://$(docker-machine ip swl-consul):8500" \
    --engine-opt="cluster-advertise=eth1:0" \
	swl-demo0

docker-machine create \
	-d virtualbox \
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
docker network create -d overlay my-net

# Check that it's on both hosts
docker network ls

# Try it out!

docker run -itd --name=web --net=my-net --env="constraint:node==swl-demo0" nginx
docker run -it --rm --net=my-net --env="constraint:node==swl-demo1" busybox wget -O- http://web
