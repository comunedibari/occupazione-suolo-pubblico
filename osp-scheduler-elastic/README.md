mvn clean install -DskipTests

docker build -t docker-registry.docker-registry.10.10.85.108.xip.io/docker-local/osp-scheduler:1.0.2 .

docker push docker-registry.docker-registry.10.10.85.108.xip.io/docker-local/osp-scheduler:1.0.2