FROM ubuntu
MAINTAINER Justin Plock <justin.plock@ge.com>

RUN echo "deb http://archive.ubuntu.com/ubuntu precise main universe" > /etc/apt/sources.list
RUN apt-get update
RUN apt-get upgrade -y

RUN apt-get install -y -q wget openjdk-7-jre-headless
RUN mkdir -p /opt/snowizard /var/log/snowizard
RUN wget -q -O /opt/snowizard/snowizard.jar http://repo.maven.apache.org/maven2/com/ge/snowizard/snowizard-service/1.0.2/snowizard-service-1.0.2.jar

ADD ./snowizard-service/snowizard.upstart /etc/init/snowizard.conf
ADD ./snowizard-service/snowizard.yml /etc/snowizard.yml
ADD ./snowizard-service/snowizard.jvm.conf /etc/snowizard.jvm.conf

ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64

# Snowizard port
EXPOSE 8080
# Administration port
EXPOSE 8180

CMD /usr/bin/java -d64 -server -jar /opt/snowizard/snowizard.jar server /etc/snowizard.yml
