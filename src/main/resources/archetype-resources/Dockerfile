#set($dollar = '$')
FROM eclipse-temurin:17-jre-jammy
VOLUME /tmp
ARG JAR_FILE
COPY ${dollar}{JAR_FILE} app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]