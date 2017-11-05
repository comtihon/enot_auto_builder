FROM openjdk:8-jdk-alpine
RUN apk update \
    && apk add curl gcc perl libc-dev make ncurses ncurses-dev openssl openssl-dev

RUN curl -O https://raw.githubusercontent.com/kerl/kerl/master/kerl \
    && chmod a+x kerl

RUN ./kerl build 19.3 19.3 \
    && ./kerl install 19.3 ~/kerl/19.3

RUN ./kerl build 20.0 20.0 \
    && ./kerl install 20.0 ~/kerl/20.0

RUN apk add python3 \
    && pip3 install coon

ENV JAVA_OPTS=""
ENV HOME="/"

COPY src src
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY gradle gradle
RUN ./gradlew build
RUN cp build/libs/coon_auto_builder-*.jar ${HOME}/app.jar

ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar ${HOME}/app.jar