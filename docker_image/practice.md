## 도커 사설 레지스트리 이용해보기

### A. 도커 사설 레지스트리 컨테이너 띄우기
1. 컨테이너의 5000번 포트를 호스트의 5001번 포트와 연결
2. 이미지 delete가 가능하도록 설정하고, 이미지 저장 위치는 /var/lib/testdir로 설정
3. 컨테이너의 /var/lib/testdir 위치를 로컬 현재 디렉터리에 마운트

```sh
$ docker run -d --name private_registry \
  -p []:[] \
  --restart=always \
  -e REGISTRY_STORAGE_DELETE_ENABLED=[] \
  -e REGISTRY_STORAGE_FILESYSTEM_ROOTDIRECTORY=[] \
  -v []:[] \
  registry:2.6

```

### B. 사설 레지스트리에 이미지 push
```sh
$ docker run -ti --name test_container ubuntu:20.04
$ echo '쓰고싶은 말' >> test.txt

# ctrl + p + q
# 컨테이너 이미지화 방법 1
$ docker [???] -o test_image.tar test_container
$ docker [???] test_image.tar test_image:1.0

# 컨테이너 이미지화 방법 2
$ docker [???] test_container test_image:1.0

# base_image와 test_image:1.0 레이어 확인
$ docker inspect ubuntu:20.04
$ docker inspect test_image:1.0

# 레지스트리 push를 위해 도커 이미지 이름 변경
$ docker [???] test_image:1.0 [ip 주소]:5001/test_image:1.0   # localhost

# push
$ docker push [ip주소]:5001/export_image:1.0  # push 실패 -> docker desktop에서 설정 바꾸기
```

### A. 사설 레지스트리에서 이미지 pull & test.txt 내용 확인
```sh
$ docker pull [ip주소]:5001/test_image:1.0
$ docker run [ip주소]:5001/test_image:1.0 [test.txt 내용 확인을 위한 커맨드 작성]
```

### api를 통해 이미지 목록 조회 & 삭제
```sh
# 이미지 목록 확인
$ curl [ip]:5001/v2/_catalog

# 이미지 태그 리스트 확인
$ curl [ip]:5001/v2/test_image/tags/list

# 이미지 상세정보 확인
$ curl -i --header "Accept: application/vnd.docker.distribution.manifest.v2+json" [ip]:5001/v2/test_image/manifests/1.0

# 이미지 삭제
## 매니페스트 삭제
$ curl --header "Accept: application/vnd.docker.distribution.manifest.v2+json" \
      -X DELETE -v \
      [ip]:5001/v2/test_image/manifests/sha256:[매니페스트 sha256 값]

## 레이어 파일 삭제
$ curl -X DELETE \
      -i [ip]:5001/v2/test_image/blobs/sha256:[레이어 sha256 값]
```