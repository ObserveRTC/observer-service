FROM openjdk:14-alpine
COPY build/libs/bigquery-reporter-*-all.jar bigquery-reporter.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "bigquery-reporter.jar"]