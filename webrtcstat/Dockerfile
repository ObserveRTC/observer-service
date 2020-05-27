#FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim
FROM adoptopenjdk/openjdk12:jdk-12.0.2_10-alpine-slim
COPY build/libs/webrtcstat-*-all.jar gatekeeper.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar gatekeeper.jar
