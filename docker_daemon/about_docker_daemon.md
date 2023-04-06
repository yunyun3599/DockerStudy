# 도커 데몬

## docker, dockerd
컨테이너나 이미지를 다루는 명령어는 `/usr/bin/docker`에서 실행  
도커 엔진의 프로세스는 `/usr/bin/dockerd` 파일로 실행  

* docker = 클라이언트로서의 도커  
  - 도커 데몬에 보낼 API를 사용할 수 있도록 CLI를 제공   
  - 도커 클라이언트는 `/var/run/docker.sock`에 위치한 유닉스 소켓을 통해 도커 데몬의 API를 호출  
* dockerd = 서버로서의 도커  
  - 실제로 컨테이너를 생성, 실행, 이미지 관리
  - 외부에서 API를 입력받아 도커 엔진의 기능을 수행
  - 도커 프로세스가 실행되어 서버로서 입력을 받을 준비가 된 상태 = **도커 데몬**

> **도커 명령어 흐름**
>1. 사용자가 CLI를 통한 명령어 입력 (e.g. docker images)
>2. /usr/bin/docker는 /var/run/docker.sock 유닉스 소켓을 사용해 도커 데몬에게 명령어를 전달
>3. 도커 데몬은 이 명령어를 파싱하고 명령어에 해당하는 작업을 수행
>4. 수행 결과를 도커 클라이언트에게 반환하고 사용자에게 결과를 출력

## 도커 데몬 실행  
도커 데몬 실행 명령어
```sh
$ service docker start
$ service docker stop
```

서비스를 사용하지 않고 직접 도커 데몬 실행하기  
```sh
$ service docker stop   # 도커 데몬 멈추기
$ dockerd               # 도커 데몬 실행 (foreground 상태로 실행됨)
```
위처럼 도커 데몬을 실행켜두고 다른 터미널에서 도커 명령어를 입력하면 도커 사용 가능

## 도커 데몬 설정  
도커 데몬 적용 옵션 확인
```sh
$ dockerd --help
```

옵션 사용 예시
```sh
$ dockerd --insecure-registry=192.168.99.100:5000
```

옵션을 직접 주어 사용해도 되고, 설정파일의 DOCKER_OPTS에 원하는 값을 입력해서 사용할 수도 있음  
```sh
$ vi /etc/default/docker
...
DOCKER_OPTS="dockerd -H tcp://0.0.0.0:2375 --insecure-registry=192.168.100.99:5000 --tls=false"
```

## 도커 데몬 제어: -H
-H 옵션은 도커 데몬의 API를 사용할 수 있는 방법을 추가함  

- 옵션 X: 도커 클라이언트`(/usr/bin/docker)`를 위한 유닉스 소켓인 `/var/run/docker.sock`을 사용
  - 아래 두 명령어 동일
    ```sh
    $ dockerd
    $ dockerd -H unix:///var/run/docker.sock
    ```
- 옵션 O: -H에 IP 주소와 포트 번호를 입력하면 원격 API인 Docker Remote API로 도커를 제어할 수 있음  
  - 로컬에 있는 도커 데몬이 아니더라도 제어할 수 있음  
  - RESTful API 형식을 사용하므로 HTTP 요청으로 도커 제어 가능  
  - 사용 명령어 예시  
    ```sh
    $ dockerd -H tcp://0.0.0.0:2375
    ```
    이 경우 `-H unix:///var/run/docker.sock`를 지정하지 않았으므로 유닉스 소켓은 비활성화되어 도커 클라이언트를 사용할 수 없게 됨. (일반적으로 Remote API와 유닉스 소켓을 동시에 설정함)

### Remote API를 사용하여 도커 데몬에 API 보내기
```sh
# 도커 데몬 실행
$ dockerd -H tcp://192.168.99.100:2375

# curl을 통한 Http 요청
$ curl 192.168.99.100:2375/version --silent | python -m json.tool
```

Remote API의 종류는 도커 명령어의 개수만큼 있으며 API에 따라서 사용하는 방법이 도커 명령어와 조금씩 다른 부분도 있음  
따라서 HTTP로 직접 API 요청을 전송하기보다는 특정 언어로 바인딩된 라이브러리를 사용하는 것이 더 일반적임  

또한 쉘의 환경변수를 설정해 원격 도커를 제어할 수도 있음  
```sh
$ export DOCKER_HOST="tcp://192.168.99.100:2375"    # 해당 도커 데몬에 API 요청을 전달
$ docker version

# 위의 설정은 아래 명령어와 동일하게 기능
$ docker -H tcp://192.168.99.100:2375 version
```

## 도커 데몬에 보안 적용
도커 데몬에 보안을 적용해두지 않으면 Remote API를 위해 바인딩된 IP 주소와 포트 번호만 알면 도커를 제어할 수 있기 때문에 보안을 적용하는 것이 바람직함  
도커 데몬에 보안을 적용하기 위해 필요한 파일은 `ca.pem, server-cert.pem, server-key.pem, cert.pem, key.pem`이 있음  

도커 데몬을 보안을 적용해서 수행시키기 위한 명령어  
```sh
$ dockerd --tlsverify \
  --tlscacert=/root/.docker/ca.pem \
  --tlscert=/root/.docker/server-cert.pem \
  --tlskey=/root/.docker/server-key.pem \
  -H=0.0.0.0:2376 \
  -H unix:///var/run/docker.sock
```
> 일반적으로 보안 미적용시 2375 포트를, 보안 적용 시 2376 포트를 사용

클라이언트는 보안이 적용된 경우 아래와 같이 요청
```sh
$ docker -H 192.168.99.100:2376 \
  --tlscacert=/root/.docker/ca.pem \
  --tlscert=/root/.docker/cert.pem \
  --tlskey=/root/.docker/key.pem \
  --tlsverify version
```

명령어에 위와 같이 옵션을 주어도 되고, 인증 관련 환경변수를 설정해 두어도 됨  
```sh
$ export DOCKER_CERT_PATH="/root/.docker
$ export DOCKER_TLS_VERIFY=1
$ docker -H 192.168.99.100:2376 version
```

curl을 통해 Remote API로 보안 적용된 도커 데몬 사용 명령어
```sh
$ curl https://192.168.99.100:2376/version \
  --cert ~/.docker/cert.pem \
  --key ~/.docker/key.pem \
  --cacert ~/.docke/ca.pem
```

## 도커 스토리지 변경: --storage-driver
**스토리지 기본값**
- 우분투(데비안 계열): overlay2  
- 구 버전의 CentOS: deviceampper

- 스토리지 설정 옵션: `--storage-driver`
- 지원 드라이버: OverlayFS, AUFS, Btrfs, Devicemapper, VFS, ZFS

적용된 스토리지 드라이버에 따라 컨테이너와 이미지가 별도로 생성됨  
```
도커가 AUFS를 기본적으로 사용하는데 devicemapper를 사용하도록 옵션을 주면 별도의 Devicemapper 컨테이너와 이미지를 사용  
-> AUFS에서 사용하던 이미지와 컨테이너 사용 불가  

별도로 생성된 Devicemapper파일은 /var/lib/docker/devicemapper 디렉터리에 저장되며, AUFS 드라이버 또한 /var/lib/docker/aufs 디렉터리에 저장됨
```

스토리지 지정 옵션을 포함한 명령어
```sh
$ dockerd --storage-driver=devicemapper
```

도커 데몬이 사용하는 컨테이너, 이미지가 저장되는 디렉터리를 별도로 지정하지 않으면 드라이버별로 사용되는 컨테이너와 이미지는 `/var/lib/docker/{드라이버명}`에 저장됨  
경로를 임의로 저장하고 싶다면 `--data-root` 옵션 설정
```sh
$ dockerd --data-root /DATA/docker
```

### 스토리지 드라이버의 원리  
실제로 컨테이너 내부에서 읽기, 새로운 파일 쓰기, 기존 파일 쓰기 작업이 일어날 때는 드라이버에 따라 `Copy-on-Write(CoW)` 또는 `Redirect-on-Write(Row)` 개념을 사용. 

스냅숏: 원본 파일은 읽기 전용으로 사용하되 이 파일이 변경되면 새로운 공간을 할당한다  
스토리지가 스냅숏으로 생성되면 스냅숏 안에 어느 파일이 어디에 저장돼 있는지가 목록으로 저장됨  
이 스냅숏을 사용하다 스냅숏 안의 파일에 변화가 생기면 변경된 내역을 따로 관리함으로써 스냅숏을 사용

**CoW**  
파일 쓰기 작업을 수행할 때 스냅숏 공간에 원본파일을 복사한 뒤 쓰기 요청을 반영  
1. 복사하기 위한 파일 읽기
2. 파일을 스냅숏 공간에 쓰고 변경된 사항을 쓰기  
-> 총 2번의 오버헤드 발생

**Row**  
한번의 쓰기 작업만 일어남  
파일을 스냅숏 공간에 복사하는 것이 아니라 스냅숏에 기록된 원본 파일은 스냅숏 파일로 묶은 후 변경된 사항을 새로운 장소에 할당받아 덮어쓰는 형식  
스냅숏 파일은 그대로 상ㅇ하되, 새로운 블록은 변경사항으로써 사용  

**도커 컨테이너 & 이미지에 적용**  
이미지 레이어 = 스냅숏에 해당  
컨테이너 = 스냅숏을 사용하는 변경점

### AUFS
데비안 계열의 기본 드라이버  
기본적으로 커널에 포함되어있지는 않으므로 RHEL, CentOS 등에서는 사용 불가  

여러 개의 이미지 레이어를 유니언 마운트 지점(union mount point)으로 제공하며 컨테이너 레이어는 여기에 마운트해서 이미지를 읽기 전용으로 사용  
- 유니언 마운트 지점: /var/lib/docker/aufs/mnt
- 컨테이너 레이어: /var/lib/docker/aufs/diff

읽기 전용 파일을 컨테이너에서 변경하려고 하면 컨테이너 레이어로 전체 파일을 복사하고 이 파일을 변경함으로써 변경사항을 반영함  
복사할 파일은 이미지의 가장 위 레이어부터 아래 레이어까지 찾기 때문에 크기가 큰 파일이 이미지이 아래 레이어에 있다면 시간이 더 오래 걸릴 수 있음  
그러나 한 번 컨테이너 레이어로 복사되고 나면 그 뒤로는 이 파일로 쓰기 작업을 수행  

**PaaS**(Platform as a Service)에 적합한 드라이버로 평가됨  
: 컨테이너의 실행, 삭제 등의 컨테이너 관련 수행 작업이 빠르기 때문  
