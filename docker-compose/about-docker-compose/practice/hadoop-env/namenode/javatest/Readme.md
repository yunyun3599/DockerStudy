### namenode 컨테이너 내에서
```sh
$ hadoop jar <생성한 jar파일> SingleFileWriteRead.파일.패키지.경로.SingleFileWriteRead input.txt Hello,Hdfs
# hadoop jar /javatest/target/javatest-1.0-SNAPSHOT.jar SingleFileWriteRead input.txt Hello,Hdfs
```
시에 hdfs 접근하는 과정이 실행되어야 함

### Error: Could not find or load main class jar 오류
pom.xml에 아래 내용 추가
```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.2.2</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>SingleFileWriteRead</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

후에 아래 명령어로 jar파일 생성
```sh
$ mvn clean
$ mvn package
```

### 도커 이미지 내의 자바 버전이 달라서 아래 문제 발생
```
Exception in thread "main" java.lang.ClassNotFoundException: src.main.java.SingleFileWriteRead
        at java.net.URLClassLoader.findClass(URLClassLoader.java:387)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
        at java.lang.Class.forName0(Native Method)
        at java.lang.Class.forName(Class.java:348)
        at org.apache.hadoop.util.RunJar.run(RunJar.java:316)
        at org.apache.hadoop.util.RunJar.main(RunJar.java:236)
```

따라서 자바 버전 변경
```sh
$ yum update
$ yum list java*jdk-devel
# 리스트 중 java-11-openjdk-devel.aarch64 설치
$ yum install -y java-11-openjdk-devel.aarch64
$ update-alternatives --config java
# 원하는 자바 버전의 Selection 번호를 입력 후 enter
```


### 추가적으로 진행할 것
hadoop 설치 시 java version 8로 진행을 했었어서 java 8을 지우고 진행하려 하니 hadoop 명령어가 실행되지 않음
yoonjae/hadoop:base 생성 가이드를 다시 보면서 java 버전을 11로 하여 다시 이미지 생성하고 진행하기!!