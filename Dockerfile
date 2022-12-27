FROM amazoncorretto:17-alpine-jdk
COPY build/libs/checkout-0.0.1-SNAPSHOT.jar checkout-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/checkout-0.0.1-SNAPSHOT.jar"]
