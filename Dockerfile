FROM alpine/java:17.0.12-jre
MAINTAINER lujian213.github.io
COPY ./dist ./eggfund
RUN echo "all ready"
CMD ["eggfund/bin/eggfund.sh"]