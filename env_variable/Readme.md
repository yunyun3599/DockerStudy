# DOCKER 컨테이너 생성 시에 환경변수 설정하기

### 방법 1
```sh
$ docker run -ti -e variable=value_of_variable ubuntu:focal env
```

## 방법 2
```sh
$ docker run -ti --env-file filename ubuntu:focal env
```

현재 디렉토리에 있는 sample_env파일을 이용하면  
```sh
$ docker run -ti --env-file sample_env ubuntu:focal env
```
 
결과로 VAR_STR과 VAR_NUM값이 나옴
