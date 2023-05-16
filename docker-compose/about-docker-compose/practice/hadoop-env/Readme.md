# hadoop 설정

## NameNode
- namenode

## DataNode
- secondarynode
- datanode01
- datanode02

<hr>  
기본 이미지: yoonjae/hadoop:base   
-> 하둡이 깔려있는 centos  

<br> <br>   
namenode 컨테이너만 Datanode를 알려주기 위해 ./namenode 디렉터리 하에 추가 설정 관련 Dockerfile 존재  
- $HADOOP_CONFIG_HOME/workers 파일에 Datanode의 hostname 기재하는 작업


## 작업 시작
```cd 작업 디렉토리 최상위 ```  
```docker-compose build```  
```docker-compose up```   


## jps를 통한 자바 프로세스 확인
터미널에 ```jps``` 커맨드 수행
- wikibooks01
    - NameNode 떠있어야됨
- wikibooks02
    - DataNode, SecondaryNameNode 떠있어야됨

## 웹 인터페이스 확인
http://localhost:50070 혹은 http://172.28.0.2:50070 를 통해 웹 인터페이스 접근 가능  
![image](https://user-images.githubusercontent.com/55429237/195596927-1a967442-0a30-4d45-abab-f67205bf4660.png)

## 작업 완료 후  
```docker-compose down```





localhost ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBBcWJekEdmg9DOx+z4XBXC7RaxBiHlY/GHrlm29S+TqWiPB3sz55Mc6JoctbUknBl2S4jjNb3bmFIOEzBL9Y4yY=
secondarynode,178.28.0.3 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBBcWJekEdmg9DOx+z4XBXC7RaxBiHlY/GHrlm29S+TqWiPB3sz55Mc6JoctbUknBl2S4jjNb3bmFIOEzBL9Y4yY=
datanode01,172.28.0.4 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBBcWJekEdmg9DOx+z4XBXC7RaxBiHlY/GHrlm29S+TqWiPB3sz55Mc6JoctbUknBl2S4jjNb3bmFIOEzBL9Y4yY=
datanode02,172.28.0.5 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBBcWJekEdmg9DOx+z4XBXC7RaxBiHlY/GHrlm29S+TqWiPB3sz55Mc6JoctbUknBl2S4jjNb3bmFIOEzBL9Y4yY=
namenode,172.28.0.2 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBBcWJekEdmg9DOx+z4XBXC7RaxBiHlY/GHrlm29S+TqWiPB3sz55Mc6JoctbUknBl2S4jjNb3bmFIOEzBL9Y4yY=
172.28.0.3 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBBcWJekEdmg9DOx+z4XBXC7RaxBiHlY/GHrlm29S+TqWiPB3sz55Mc6JoctbUknBl2S4jjNb3bmFIOEzBL9Y4yY=
~                        