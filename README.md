# 1-й вариант запуска:

* Клонировать проект
* Открыть его(желательно jetbrains IDEA Ultimate)
* Синхронизировать Gradle
* Заполнить prod.env или test.env(тестовое необязательно)(есть example.env)
* Можно запускать сервер

### При удачном запуске будет:

2025-04-29 15:18:51.557 [main] INFO  [Koin] - Started 6 definitions in 0.7945 ms  
2025-04-29 15:18:52.158 [main] INFO Application - Application started in 1.164 seconds.  
2025-04-29 15:18:52.620 [main] INFO Application - Responding at http://localhost:8080

# 2-й вариант запуска:

* Также клонировать проект
* Иметь docker
* Запустить с помощью команды

## Запуск в docker

```shell
  docker compose --env-file prod.env up --build -d
```

### Остановить с помощью

```shell
   docker compose --env-file prod.env down
```

## P.S

Также есть тесты(хоть и мало)