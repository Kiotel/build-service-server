# Запуск

Для этого надо:

* Склонировать проект
* Открыть его(желательно jetbrains IDEA Ultimate)
* Синхронизировать Gradle
* Заполнить prod.env или test.env(есть example.env)
* Запустить бд в ./db/prod (Там тоже есть readme)
* Можно запускать сервер

## При удачном запуске будет:

2025-04-29 15:18:51.557 [main] INFO  [Koin] - Started 6 definitions in 0.7945 ms  
2025-04-29 15:18:52.158 [main] INFO Application - Application started in 1.164 seconds.  
2025-04-29 15:18:52.620 [main] INFO Application - Responding at http://localhost:8080

Также есть тесты, для них нужно запускать ./db/test