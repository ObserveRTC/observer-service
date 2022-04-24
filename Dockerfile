FROM gradle:7.3.3-jdk17 AS BUILDER

COPY ./ ./

RUN gradle build --no-daemon -x test
RUN #gradle build --no-daemon

FROM openjdk:17-ea-22-jdk-oraclelinux8
# openjdk:19-ea-jdk

WORKDIR /home/gradle
COPY --from=BUILDER /home/gradle/build/docker/main/layers/libs /home/app/libs
COPY --from=BUILDER /home/gradle/build/docker/main/layers/resources /home/app/resources
COPY --from=BUILDER /home/gradle/build/docker/main/layers/application.jar /home/app/application.jar
#ENV MICRONAUT_CONFIG_FILES=/observer-config.yaml

EXPOSE 7080
EXPOSE 7081

ENTRYPOINT ["java", "-jar", "/home/app/application.jar"]
