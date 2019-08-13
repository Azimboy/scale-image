FROM openjdk:8
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin

ENV SBT_VERSION  1.2.8
ENV APP_NAME     scale-image
ENV APP_VERSION  1.0

# Install curl
RUN \
  apt-get update && \
  apt-get -y install curl && \
  apt-get -y install vim

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get -y install sbt

# Install node and npm
RUN curl -sL https://deb.nodesource.com/setup_9.x | bash && \
    apt-get install -y nodejs npm build-essential

# Define working directory
WORKDIR /root
ENV PROJECT_HOME /usr/src

COPY ["build.sbt", "/tmp/build/"]
COPY ["project/plugins.sbt", "project/build.properties", "/tmp/build/project/"]
RUN cd /tmp/build && \
 sbt update && \
 sbt compile

RUN mkdir -p $PROJECT_HOME/app

WORKDIR $PROJECT_HOME/app

COPY . $PROJECT_HOME/app

RUN sbt test dist

# We are running play on this port so expose it
EXPOSE 9000

RUN unzip target/universal/$APP_NAME-$APP_VERSION.zip
RUN chmod +x $APP_NAME-$APP_VERSION/bin/$APP_NAME
ENTRYPOINT $APP_NAME-$APP_VERSION/bin/$APP_NAME