# Инструкция к docker'у

## запускать с помощью

```shell
  docker-compose --env-file ../../prod.env up -d
```

## проверить запуск

```shell
   docker-compose --env-file ../../prod.env ps
```

## остановить с помощью

```shell
   docker compose --env-file ../../prod.env down
```