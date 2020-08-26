FROM gradle:6.3-jdk14 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle :observer:build --no-daemon 

#FROM openjdk:14-alpine
FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim
COPY --from=build /home/gradle/src/observer/build/libs/observer-*-all.jar observer.jar
#COPY build/libs/bigquery-reporter-*-all.jar bigquery-reporter.jar
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "observer.jar"]