FROM debian:jessie
MAINTAINER Justin Plock <justin@plock.net>

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && apt-get install -y -q wget openjdk-7-jre-headless
RUN mkdir -p /opt/snowizard /var/log/snowizard
RUN wget -q -O /opt/snowizard/snowizard.jar http://repo.maven.apache.org/maven2/com/ge/snowizard/snowizard-application/1.4.0/snowizard-application-1.4.0.jar

ADD ./snowizard-application/snowizard.upstart /etc/init/snowizard.conf
ADD ./snowizard-application/snowizard.yml /etc/snowizard.yml
ADD ./snowizard-application/snowizard.jvm.conf /etc/snowizard.jvm.conf

ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64

# Snowizard port
EXPOSE 8080
# Administration port
EXPOSE 8180

WORKDIR /opt/snowizard

ENTRYPOINT ["java", "-d64", "-server", "-jar", "snowizard.jar"]
CMD ["server", "/etc/snowizard.yml"]
