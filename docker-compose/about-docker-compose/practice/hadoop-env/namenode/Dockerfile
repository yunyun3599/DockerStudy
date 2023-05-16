FROM yoonjae/hadoop:base

WORKDIR /

ARG HADOOP_CONFIG_HOME=/hadoop_home/hadoop-3.3.4/etc/hadoop

COPY masters ${HADOOP_CONFIG_HOME}/masters
COPY workers ${HADOOP_CONFIG_HOME}/workers
COPY start.sh /start.sh
RUN chmod +x /start.sh

COPY javatest /javatest
