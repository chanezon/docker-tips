# Running Graphical applications in Docker for Mac

<img src="/img/scatter-plot.png"/>

## Setup

Docker is used a lot to build, ship and run server-side microservices. But one of the undervalued benefits of containerization is that it allows you to containerize the development process as well. Application development involves a lot of yak-shaving: install a runtime, an IDE, a database, configure it all, repeat and rinse for each app and all their versions, using different versions of runtimes eventually (rvm for Ruby, nvm for Node, VirtualEnv for Python, ...). This post is about how to containerize your development workflow, including using graphical apps such as IDEs, editors, Image and vector graphics editors or scientific programs. Developers on Linux were able to do that since [Jessie Frazelle's series of blog posts](https://blog.jessfraz.com/post/docker-containers-on-the-desktop/) and [talks](https://www.youtube.com/watch?v=GsLZz8cZCzc), mounting the X11 socket inside the container: this post is about how to leverage these tools from Docker for Mac.

Docker for Mac lets you run any Linux executable in an isolated process on Mac. A graphical app is just another process, that needs access to the X11 socket of the system, or an X11 server. You can run X11 applications on a Mac using an open source project called [Xquartz](http://www.xquartz.org/). The steps to expose XQuartz to a Linux process running in Docker are simple:
1. install XQuartz from [xquartz.org](http://www.xquartz.org/)
Then you have 3 choices:
1. Proxy the XQuartz socket to port 6000
or
2. Tell Xquartz to accept network calls. This is not very secure.
3. Tell Xquartz to accept network calls and require authentication, setup X11 security using xauth, and mount ~/.Xauthority in the container.

Thus, after you install XQuartz, the 3 methods are as follows.

### Proxying

In your .bashrc:
```
export DISPLAY_MAC=`ifconfig en0 | grep "inet " | cut -d " " -f2`:0

function startx() {
	if [ -z "$(ps -ef|grep XQuartz|grep -v grep)" ] ; then
	    open -a XQuartz
        socat TCP-LISTEN:6000,reuseaddr,fork UNIX-CLIENT:\"$DISPLAY\" &
	fi
}
```

Create a container using X11:
```
startx
docker run -e DISPLAY=$DISPLAY_MAC -it jess/geary
```

### Exposing X11 on the network, with no authentication

This approach is insecure, especially if you don't use a firewall on your machine. For a more secure approach see the next section.

In your .bashrc:
```
export DISPLAY_MAC=`ifconfig en0 | grep "inet " | cut -d " " -f2`:0
defaults write org.macosforge.xquartz.X11 nolisten_tcp -boolean false

function startx() {
	if [ -z "$(ps -ef|grep XQuartz|grep -v grep)" ] ; then
	    open -a XQuartz
	fi
}
```

Create a container using X11:
```
startx
docker run -e DISPLAY=$DISPLAY_MAC -it jess/geary
```

### Exposing X11 on the network, with authentication

Launch XQuartz and in security settings, set authenticate connexions and expose on network.

<img src="/img/xquartz-settings-auth.png"/>

In a Terminal, list the magic cookies that have been set, and add one for the Docker VM bridhe IP. 
```
$ export DISPLAY_MAC=`ifconfig en0 | grep "inet " | cut -d " " -f2`:0
$ xauth list
pc34.home/unix:0  MIT-MAGIC-COOKIE-1  491476ce33cxxx86d4bfbcea45
pc34.home:0  MIT-MAGIC-COOKIE-1  491476ce33cxxx86d4bfbcea45
$ export DISPLAY=$DISPLAY_MAC
$ xauth
Using authority file /Users/pat/.Xauthority
xauth> add 192.168.64.1:0 . 491476ce33cxxx86d4bfbcea45
xauth> exit
Writing authority file /Users/pat/.Xauthority
$ xauth list
pc34.home/unix:0  MIT-MAGIC-COOKIE-1  491476ce33cxxx86d4bfbcea45
pc34.home:0  MIT-MAGIC-COOKIE-1  491476ce33cxxx86d4bfbcea45
192.168.64.1:0  MIT-MAGIC-COOKIE-1  491476ce33cxxx86d4bfbcea45
pc34:docker-tips pat$ docker run -e DISPLAY=$DISPLAY_MAC -v ~/.Xauthority:/root/.Xauthority -it jess/gimp

```

In your .bashrc:
```
export DISPLAY_MAC=`ifconfig en0 | grep "inet " | cut -d " " -f2`:0
defaults write org.macosforge.xquartz.X11 nolisten_tcp -boolean false

function startx() {
	if [ -z "$(ps -ef|grep XQuartz|grep -v grep)" ] ; then
	    open -a XQuartz
	fi
}
```

Create a container using X11:
```
startx
docker run -e DISPLAY=$DISPLAY_MAC -v ~/.Xauthority:/root/.Xauthority -it jess/geary
```

### Troubleshooting

Checking that port 6000 is exposed.
```
lsof -i :6000
```

Checking XQuartz / Preferences / Security / "Allow connections from network clients" should be checked if you use option 2.

## Examples

The examples assume you have started XQuartz (xstart) and that you have setup the DISPLAY_MAC environment variable. 

Examples are shown without authentication. Just add -v ~/.Xauthority:/root/.Xauthority (or mount it under the home directory of the user in the Docker image that is used to start the process).

### Spring Tool Suite

When you run STS in a container, first create a maven volume. Volume mount is still slower on Mac than native access and using a single maven volume for all your maven-related containers will allow these containers to share a single maven cache while getting the best read performance. Create a directory on your Mac to host your STS workspace, and mount it in the container. You can check-out the source inside that directory, then import it in STS (File -> Import -> Existing maven project), or check it our from STS (File -> Import -> Maven project from SCM, in which case you also need to mount your Git credentials). If your app is using MongoDB or other services, launch them as containers with a name before STS, then link them to the STS container: you will need to set your Run configuration to the alias you have set for the links for your app to talk to these services. This whole setup can be automated with a compose file. Last, expose the ports for the app that you want to test in your browser or with other tools: these will be available from your mac at `docker.local:port`. This example is designed to develop the [spring-doge app](https://github.com/chanezon/spring-doge), which uses Mongodb, and a complete docker-compose.yml for it is available at  [spring-doge compose file](https://github.com/chanezon/spring-doge/blob/master/docker-compose.dev.yml).

When used with the right compose file, this setup allows a new developer in the team to start developing and debugging a complex Java app with `git clone` and `docker-compose up`: no more long `README` files, no more complex setup and yak shaving:-)
 
```
docker run -name mongo -d mongo
docker volume create maven
docker run -e DISPLAY=$DISPLAY_MAC -it \
--rm \
-v $HOME/code/sts1:/workspace \
-v maven:/root/.m2 \
--name sts-doge \
--link mongo:mongo \
-p 8080:8080 \
chanezon/sts:latest
```

<img src="/img/sts.png"/>

### Eclipse

Leesah created a container for the latest Eclipse Mars version. In this image, you have to override the entrypoint in order to pass the workspace you just mounted as a parameter.
```
docker run -e DISPLAY=$DISPLAY_MAC -it \
-v $HOME/code/eclipse:/work \
--entrypoint /usr/local/eclipse/eclipse \
leesah/eclipse \
-data /work
```

<img src="/img/eclipse-mars.png"/>

Tiokksar maintains a container for Eclipse Luna. 
```
docker run -e DISPLAY=$DISPLAY_MAC -it \
-v $HOME/code/eclipse-luna:/home/dev/eclipse \
tiokksar/eclipse \
eclipse -data /home/dev/eclipse
```

<img src="/img/eclipse-luna.png"/>

### Gimp

Jess' Gimp image. Mount a local volume if you want to edit images from your laptop. 
```
docker run -e DISPLAY=$DISPLAY_MAC -it jess/gimp
```

<img src="/img/gimp.png"/>

### Inkscape

Jess' Inkspace image. Mount a local volume if you want to edit SVG files from your laptop. 
```
docker run -e DISPLAY=$DISPLAY_MAC -it rasch/inkscape
```

<img src="/img/inkscape.png"/>

### FireFox

Jess' Firefox image. Second example mounts a local directory as a volume and opens a file from it.
```
docker run -e DISPLAY=$DISPLAY_MAC -it jess/firefox 
docker run -e DISPLAY=$DISPLAY_MAC -it \ 
-v $PWD:/foo jess/firefox file:///foo/sinewave.gif
```

<img src="/img/firefox.png"/>

### Chrome

Jess' Chrome image does not work for me. I need to investigate what the issue is.
```
docker run -e DISPLAY=$DISPLAY_MAC -it jess/chrome 
```

### Gnu Octave

Read [Cameron Taggart's blog post](http://blog.ctaggart.com/2016/03/gnu-octave-via-docker-x11.html) and look at [Victoria Lynn's repo](https://github.com/VictoriaLynn/plotting-examples)for Octave and SciPy.
```
git checkout https://github.com/VictoriaLynn/plotting-examples
cd plotting-examples
mkdir octave
docker run -e DISPLAY=$DISPLAY_MAC -it \
-v $PWD:/scripts/plotting-examples \
-v $PWD/octave:/root/.config/octave \
epflsti/octave-x11-novnc-docker:latest octave
```

<img src="/img/octave.png"/>

### SciPy

Read [Cameron Taggart's blog post](http://blog.ctaggart.com/2016/03/gnu-octave-via-docker-x11.html) and look at [Victoria Lynn's repo](https://github.com/VictoriaLynn/plotting-examples)for Octave and SciPy.

This sample plots a 3D diagram using SciPy, using X11 for the graphics. Since SciPy is not included in the Octave image, we'll render it in the excellent jupyter/scipy-notebook image. First we create a container with the image, then we docker exec into it and render the diagram.
```
git checkout https://github.com/VictoriaLynn/plotting-examples
cd plotting-examples
docker run -e DISPLAY=$DISPLAY_MAC -it \
--name 3drender \
-p 8888:8888 -v $PWD:/scatter jupyter/scipy-notebook
docker exec -it 3drender bash -c "cd /scatter/3D-scatter/ && source activate python2 && python scatter_3D.py"
```

<img src="/img/scatter-plot.png"/>

### Credits / Additional resources

Jessie Frazelle's blog post, talks and repos, are invaluable. I highly recommend looking at her [Dockerfiles repo](https://github.com/jfrazelle/dockerfiles).

[Benny Cornelissen's post was super useful](http://blog.bennycornelissen.nl/bwc-gui-apps-in-docker-on-osx/) for the Mac side. 

[Cameron Taggart's blog post](http://blog.ctaggart.com/2016/03/gnu-octave-via-docker-x11.html) and [Victoria Lynn's repo](https://github.com/VictoriaLynn/plotting-examples) were excellent for Octave and SciPy.

 
