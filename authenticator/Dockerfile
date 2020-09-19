FROM openjdk:14-alpine
COPY build/libs/authenticator-*-all.jar authenticator.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "authenticator.jar"]