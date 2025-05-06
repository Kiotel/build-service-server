# Инструкция к docker'у

## Запускать с помощью

```shell
  docker-compose --env-file ../../prod.env up -d
```

## Проверить запуск

```shell
   docker-compose --env-file ../../prod.env ps
```

## Остановить с помощью

```shell
   docker compose --env-file ../../prod.env down
```