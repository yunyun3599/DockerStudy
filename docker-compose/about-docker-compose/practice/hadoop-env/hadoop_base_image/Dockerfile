FROM ubuntu:20.04

RUN apt-get update && apt-get upgrade
RUN apt-get install -y wget
RUN apt-get install -y vim
RUN apt-get install -y openssh-server openssh-client
RUN apt-get install -y openjdk-11-jdk

ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-arm64/

ENV HDFS_NAMENODE_USER="root"
ENV HDFS_DATANODE_USER="root"
ENV HDFS_SECONDARYNAMENODE_USER="root"
ENV YARN_RESOURCEMANAGER_USER="root"
ENV YARN_NODEMANAGER_USER="root"

RUN ssh-keygen -t rsa -P '' -f ~/.ssh/id_dsa
RUN cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys
# RUN ssh-keygen -f /etc/ssh/ssh_host_rsa_key -t rsa -N ""
# RUN ssh-keygen -f /etc/ssh/ssh_host_ecdsa_key -t ecdsa -N ""
# RUN ssh-keygen -f /etc/ssh/ssh_host_ed25519_key -t ed25519 -N "" 

RUN echo secondarynode,172.28.0.3 $(cat /etc/ssh/ssh_host_ecdsa_key.pub  | cut --delimiter=" " --fields=-2) >> ~/.ssh/known_hosts
RUN echo datanode01,172.28.0.4 $(cat /etc/ssh/ssh_host_ecdsa_key.pub  | cut --delimiter=" " --fields=-2) >> ~/.ssh/known_hosts
RUN echo datanode02,172.28.0.5 $(cat /etc/ssh/ssh_host_ecdsa_key.pub  | cut --delimiter=" " --fields=-2) >> ~/.ssh/known_hosts

RUN mkdir /hadoop_home
RUN cd /hadoop_home \
    && wget "https://dlcdn.apache.org/hadoop/common/stable/hadoop-3.3.4.tar.gz" \
    && tar xvfz hadoop-3.3.4.tar.gz \
    && ln -sfn hadoop-3.3.4 hadoop

ENV HADOOP_HOME=/hadoop_home/hadoop-3.3.4
ENV HADOOP_CONFIG_HOME=/hadoop_home/hadoop-3.3.4/etc/hadoop
ENV PATH=$PATH:/hadoop_home/hadoop-3.3.4/bin
ENV PATH=$PATH:/hadoop_home/hadoop-3.3.4/sbin

RUN echo "export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-arm64/" >> /hadoop_home/hadoop/etc/hadoop/hadoop-env.sh

COPY core-site.xml /hadoop_home/hadoop-3.3.4/etc/hadoop/core-site.xml
COPY hdfs-site.xml /hadoop_home/hadoop-3.3.4/etc/hadoop/hdfs-site.xml
COPY mapred-site.xml /hadoop_home/hadoop-3.3.4/etc/hadoop/mapred-site.xml

RUN mkdir /hadoop_home/temp
RUN mkdir /hadoop_home/namenode_dir
RUN mkdir /hadoop_home/datanode_dir

RUN echo localhost $(cat ssh_host_ecdsa_key.pub  | cut --delimiter=" " --fields=-2) >> ~/.ssh/known_hosts

RUN hadoop namenode -format
RUN echo "#!/bin/bash" >> /entrypoint.sh
RUN echo "service ssh start" >> /entrypoint.sh
RUN echo "/bin/bash" >> /entrypoint.sh
