FROM yoonjae/centos:hadoop

WORKDIR /

ARG HADOOP_CONFIG_HOME=/hadoop_home/hadoop-2.7.7/etc/hadoop

COPY slaves ${HADOOP_CONFIG_HOME}/slaves
COPY masters ${HADOOP_CONFIG_HOME}/masters
COPY hdfs-site.xml ${HADOOP_CONFIG_HOME}/hdfs-site.xml
