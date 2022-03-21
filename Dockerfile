FROM openjdk:17
VOLUME /tmp
ARG JAR_FILE

COPY ${JAR_FILE} app.jar
CMD java -Djava.security.egd=file:/dev/./urandom -Dserver.port=$PORT -Dspring.profiles.active=heroku -jar /app.jar
