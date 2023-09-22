@echo off
set progressiveId=%1
IF "%~1" == "" GOTO emptyParameter
docker build -t docker-registry.docker-registry.10.10.85.108.xip.io/osp-fe:2.3.%progressiveId% --build-arg PROFILE=sviluppo .
docker push docker-registry.docker-registry.10.10.85.108.xip.io/osp-fe:2.3.%progressiveId%
docker build -t docker-registry.docker-registry.10.10.85.108.xip.io/osp-fe-coll:2.3.%progressiveId% --build-arg PROFILE=collaudo .
docker push docker-registry.docker-registry.10.10.85.108.xip.io/osp-fe-coll:2.3.%progressiveId%

:emptyParameter:
  echo No latest tag. Example:
  echo build-dev-coll.bat 52
