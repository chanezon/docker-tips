# Using docker-machine to try out the latest versions of Docker

This is part of the [P@ Docker Tips](/README.md) series or tips about Docker.

Today's tip features @nathanleclaire & @ehazlett from the docker-machine team, playing the trumpet, @jfrazelle on base & @tianon on drums.

<img src="/img/tip1-docker-machine.png"/>

Recently @jfrazelle announced [availability of Docker 1.6.0-rc3](https://groups.google.com/forum/#!topic/docker-dev/tc5zaVcSRrU). I'd like to try it out, but don't want to mess with the stable Docker version I installed on my Mac that I use for development. 

With the latest version of docker-machine (as of posting this you have to run a build of head) you can now use the --virtualbox-boot2docker-url to specify the url of an iso from which to create the image for boot2docker. It so happens that @tianon announced a boot2docker image for  1.6.0-rc3 right away, at https://github.com/tianon/boot2docker/releases/download/v1.6.0-rc3/boot2docker.iso. As of today, the latest version is 1.6.0-rc4, for which @nathanleclaire has posted an iso at https://s3-us-west-1.amazonaws.com/b2diso/rc4.iso 

In order to run this tip you need to install docker-machine HEAD build on your Mac.
```
curl https://docker-machine-builds.evanhazlett.com/latest/docker-machine_darwin_amd64 > $(which docker-machine)
chmod +x /usr/local/bin/docker-machine
docker-machine -v
docker-machine version 0.2.0 (HEAD)
```

Then create a docker-machine with boot2docker 1.6.0-rc4 image,and ssh to it.
```
docker-machine -D create \
-d virtualbox \
--virtualbox-boot2docker-url https://s3-us-west-1.amazonaws.com/b2diso/rc4.iso \
rc4
docker-machine ssh rc4
docker -v
Docker version 1.6.0-rc4...
```

You're good to go with a local VM where you can test the latest release of Docker.
