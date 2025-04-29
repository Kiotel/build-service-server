# Инструкция к docker'у

## запускать с помощью

```shell
  docker-compose --env-file ../../test.env up -d
```

## проверить запуск

```shell
   docker-compose -env-file ../../test.env ps
```

## остановить с помощью

```shell
   docker compose -env-file ../../test.env down
```