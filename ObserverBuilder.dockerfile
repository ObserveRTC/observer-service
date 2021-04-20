FROM gradle:6.7-jdk14 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
#RUN gradle clean build --no-daemon
RUN gradle clean build

#FROM openjdk:14-alpine
FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim
COPY --from=build /home/gradle/src/build/libs/observer-[0-9].[0-9].[0-9].jar observer.jar
CMD ["java", "-jar", "observer.jar"]
