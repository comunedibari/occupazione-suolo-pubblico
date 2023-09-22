FROM maven:3.8.4-openjdk-11
WORKDIR /osp-be-scheduler
COPY . .
RUN mvn clean install -DskipTests
CMD mvn spring-boot:run