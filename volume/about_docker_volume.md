# 도커 볼륨

## 기본 내용
docker 이미지로 컨테이너를 생성하면 이미지는 읽기 전용이므로 변경 사항은 각 컨테이너가 가지고 있음  
따라서 이미지는 변경되지 않으며 컨테이너 계층에 원래 이미지에서 변경된 파일시스템 등을 저장함  

여기에서 단점이 밞생하는데, 바로 컨테이너를 삭제하면 작업한 내용이 날아가게 된다는 것임   
이렇게 데이터가 휘발되는 문제를 방지하기 위해 컨테이너의 데이터를 영속적으로 사용하는 방법을 몇 가지 활용할 수 있음   
그 중 가장 활용하기 쉬운 것이 바로 `볼륨`을 활용하는 것

## 볼륨 종류
### 호스트 볼륨
호스트의 특정 디렉터리와 컨테이너의 디렉터리를 공유하는 것.  
각 위치에서 변경 사항이 양측에 모두 반영됨

아래 2개 컨테이너 생성
```sh
$ docker run -d \
  --name wordpressdb_hostvolume \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=wordpress \
  -v /Users/yoonjae/study/docker-study/volume/mysql_volume:/var/lib/mysql \
  mysql

```

```sh
$ docker run -d \
  --name wordpress_hostvolume \
  --link wordpressdb_hostvolume:mysql \
  -e WORDPRESS_DB_PASSWORD=password \
  -p 80 \
  wordpress
```
컨테이너 생성 후에 공유된 호스트의 디렉터리 경로인 `/Users/yoonjae/study/docker-study/volume/mysql_volume`에 가서 파일 목록을 확인해보면 데이터베이스 관련 파일들이 있는 것을 확인할 수 있음   
이 파일들을 컨테이너 삭제 후에도 계속 지속됨  

디렉터리 뿐만 아니라 공유는 단일 파일도 공유 가능함  
동시에 여러 개의 -v 옵션을 사용하는 것도 가능함   

이미지에 원래 존재하던 디렉터리에 호스트의 볼륨을 공유하면 컨테이너의 디렉터리가 덮어씌워짐
  - 호스트에 이미 디렉터리 및 파일이 존재하고 컨테이너에도 존재하면 컨테이너에 존재하던 내용은 호스트 볼륨의 내용으로 덮어씌워짐


### 볼륨 컨테이너
볼륨을 사용하는 컨테이너를 다른 컨테이너와 공유하는 방법  
컨테이너를 생성할 때 `--volumes-from` 옵션을 설정하면 `-v`나 `--volume` 옵션을 적용한 컨테이너의 볼륨 디렉터리를 공유할 수 있음  
```sh
$ docker run -ti --name volume_override -v /Users/yoonjae/study/docker-study/volume/test_volume:/home/testdir_2 alicek106/volume_test
$ docker run -ti --name volumes_from_container --volumes-from volume_override ubuntu:14.04
```
위에서는 `volumes_from_container` 컨테이너가 볼륨 컨테이너로 `volume_override` 컨테이너를 사용하고 있으며 `volume_override` 컨테이너는 호스트 볼륨을 마운트한 상태    
하나의 볼륨 컨테이너를 여러 컨테이너에서 공유해 사용하는 것도 가능  

### 도커가 관리하는 볼륨
따로 호스트의 볼륨을 공유하거나 볼륨 컨테이너를 사용하지 않고 도커 자체에서 제공하는 볼륨 기능을 사용하는 경우   
`docker volume` 명령어를 사용

```sh
$ docker volume create --name test_volume  # test_volume이라는 도커 볼륨 생성
```

기본적으로 제공되는 드라이버는 `local`로 이 볼륨은 로컬 호스트에 저장되며 도커 엔진에 의해 생성되고 삭제됨  

도커 볼륨을 사용하는 컨테이너 실행은 아래와 같이 수행
```sh
# 도커 볼륨을 사용하는 컨테이너 실행
$ docker run -ti --name test_volume_container_1 -v test_volume:/root/ ubuntu:20.04

# 컨테이너 내부 진입 및 볼륨에 파일 생성
$ echo test test_volume >> /root/volume.txt

# 컨테이너 나오기 ctrl + c

# 동일 볼륨을 사용하는 새로운 컨테이너 실행
$ docker run -ti --name test_volume_container_2 -v test_volume:/tmp ubuntu:20.04

# 컨테이너 내부 진입 및 위에서 생성한 volume.txt 파일이 있는 지 확인 (이 컨테이너는 볼륨을 /tmp 위치에 마운트했으므로 /tmp/volume.txt 위치에 파일이 있어야됨)
$ ls -al /
```

도커 볼륨 내의 내용도 실제로는 호스트에 저장되는데, 만약 위치를 알고 싶다면 inspect 명령어를 사용하면 됨
```sh
$ docker inspect --type volume test_volume
# inspect 명령어는 볼륨뿐 아니라 image, container 등 다양한 사항을 확인할 때 사용되므로 --type 옵션을 통해 어떤 종류를 확인하고 싶은 지 명확하게 하면 더 좋음
```
결과
```json
[
    {
        "CreatedAt": "2023-03-07T14:22:59Z",
        "Driver": "local",
        "Labels": {},
        "Mountpoint": "/var/lib/docker/volumes/test_volume/_data",
        "Name": "test_volume",
        "Options": {},
        "Scope": "local"
    }
]
```
- Driver: 볼륨이 쓰는 드라이버
- Labels: 볼륨 구분을 위한 라벨
- Mountpoint: 호스트의 어디에 저장되었는지


도커 볼륨을 컨테이너 실행 시점에서 자동으로 생성하고 할당하려면 컨테이너에서 공유할 디렉터리의 위치를 -v 옵션에 입력하면 됨
```sh
$ docker run -ti --name volume_auto -v /root ubuntu:20.04
```
위 명령어 수행 후 `docker volume ls`를 통해 목록을 확인하면 무작위로 생성되 이름으로 가진 도커 볼륨이 생성되어있음을 확인 가능
```sh
$ docker inspect volume_auto
```
위 명령어를 통해 나온 상세 정보의 mounts 부분을 확인하면 어떤 도커볼륨을 사용중인지 확인 가능

컨테이너 종료 후에 사용되지 않을 볼륨들은 자동으로 삭제되지 않음   
따라서 아래 명령어를 통해 사용되지 않는 볼륨들을 한 번에 삭제 가능  
```sh
$ docker volume prune
```

### 정리
- stateless: 외부에 데이터를 저장하고 컨테이너는 그 데이터로 동작하도록 설계하는 것 (바람직)  
- stateful: 컨테이너가 데이터를 저장하고 있어 상태가 있는 경우 (지양하는 것이 좋음)  

> 참고로 -v 대신 --mount 옵션을 사용해도 동일하게 동작  
단, 볼륨의 정보를 나타내는 방법이 다름  
```sh
# 도커 볼륨 사용하는 경우 (type=volume)
$ docker run -ti --name mount_option_docker_volume --mount type=volume,source=test_volume,target=/root ubuntu:20.04

# 호스트 볼륨 사용하는 경우 (type=bind)
$ docker run -ti --name mount_option_host_volume --mount type=bind,source=/host/local/path,target=/root ubuntu:20.04
```