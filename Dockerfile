FROM alpine/java:17.0.12-jre
COPY ./dist/lib/eggfund-1.0.0.jar eggfund-1.0.0.jar
RUN echo "all ready"
EXPOSE 9011
ENTRYPOINT ["java", "-jar", "eggfund-1.0.0.jar"]