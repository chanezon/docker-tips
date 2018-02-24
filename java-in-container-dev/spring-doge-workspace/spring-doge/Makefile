jar :
	docker run -it --rm \
-v $(PWD):/usr/src/spring-doge \
-v maven:/root/.m2 \
-w /usr/src/spring-doge \
maven:3.3-jdk-8 \
mvn package

image :
	docker build -f Dockerfile.dev \
	-t chanezon/spring-doge .

service :
	docker network create -d overlay dogenet
	docker service create --name mongo \
--network dogenet \
mongo
	sleep 3
	docker service create --name doge \
--network dogenet \
--publish 8080:8080 \
--replicas 3 \
--env MONGODB_URI=mongodb://mongo:27017/test \
chanezon/spring-doge

service_clean :
	docker service rm doge
	docker service rm mongo
	docker network rm dogenet