# Entrypoint와 CMD
### Entrypoint
- 도커 컨테이너가 실행될 때 고정적으로 실행되어야 하는 스크립트 or 명령어

### Command
- 단독 사용 시에는 도커 컨테이너가 실행될 때 수행할 명령어
- entrypoint와 함께 사용 시 엔트리포인트에 지정된 명령어에 대한 인자 값  


## 디렉터리의 예제 실습 방법
```sh
docker build -t test .
docker run test
```
