FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim
COPY build/libs/observer-*-all.jar webtc_observer.jar
EXPOSE 8088
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar webtc_observer.jar
