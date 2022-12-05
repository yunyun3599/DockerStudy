도커 컴포즈 프로젝트 시작
```sh
docker compose up -d
```

docker compose 프로젝트 종료
```sh
docker compose down
```

위의 명령어로는 volume은 제거되지 않음
volume까지 제거하기 위해서는
```sh
docker compose down -v
```