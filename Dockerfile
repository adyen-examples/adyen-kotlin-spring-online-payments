FROM amazoncorretto:17-alpine-jdk
COPY build/libs/adyen-kotlin-spring-online-payments-0.0.1-SNAPSHOT.jar checkout-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/checkout-0.0.1-SNAPSHOT.jar"]
