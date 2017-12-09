FROM comtihon/alpine_erlang

RUN apk update  \
    && apk add python3 \
    && pip3 install coon

COPY src src
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY gradle gradle
RUN ./gradlew build -i
RUN cp build/libs/coon_auto_builder-*.jar ${HOME}/app.jar

ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar ${HOME}/app.jar