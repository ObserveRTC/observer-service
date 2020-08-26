FROM gradle:6.3-jdk14 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle :observer:build --no-daemon 

FROM openjdk:14-alpine
COPY --from=build /home/gradle/src/bigquery-reporter/build/libs/bigquery-reporter-*-all.jar bigquery-reporter.jar
#COPY build/libs/bigquery-reporter-*-all.jar bigquery-reporter.jar
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "bigquery-reporter.jar"]