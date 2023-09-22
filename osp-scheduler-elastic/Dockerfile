FROM openjdk:11

LABEL maintainer="almaviva.it"

VOLUME /opt/osp-scheduler

WORKDIR /osp-scheduler

ARG JAR_FILE=target/osp-scheduler-1.0.jar

ADD ${JAR_FILE} osp-scheduler.jar

ENTRYPOINT ["java","-jar","osp-scheduler.jar"]