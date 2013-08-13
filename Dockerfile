FROM ubuntu

MAINTAINER Justin Plock <justin.plock@ge.com>

RUN sed 's/main$/main universe/' -i /etc/apt/sources.list
RUN DEBIAN_FRONTEND=noninteractive apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get upgrade -y -q

RUN DEBIAN_FRONTEND=noninteractive apt-get install -y -q wget openjdk-7-jre-headless
RUN mkdir -p /opt/snowizard /var/log/snowizard
RUN wget -q -O /opt/snowizard/snowizard.jar http://repo.maven.apache.org/maven2/com/ge/snowizard/snowizard-service/1.0.1/snowizard-service-1.0.1.jar

ADD ./snowizard-service/snowizard.upstart /etc/init/snowizard.conf
ADD ./snowizard-service/snowizard.yml /etc/snowizard.yml
ADD ./snowizard-service/snowizard.jvm.conf /etc/snowizard.jvm.conf

ENV JAVA_HOME /usr/lib/jvm/default-java

# Snowizard port
EXPOSE 8080
# Administration port
EXPOSE 8180

ENTRYPOINT /usr/bin/java -d64 -server -jar /opt/snowizard/snowizard.jar server /etc/snowizard.yml
