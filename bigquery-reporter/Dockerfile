FROM openjdk:14-alpine
COPY build/libs/bigquery-reporter-*-all.jar bigquery-reporter.jar
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "bigquery-reporter.jar"]