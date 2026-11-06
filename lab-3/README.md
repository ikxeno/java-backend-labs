# Лабораторная 3: web и тесты

Вариант 15: REST API для `Airplane(id, model, seats)`. Контроллер вызывает
сервис, сервис работает через интерфейс JDBC-репозитория. Код сущности,
репозитория и миграции скопирован из лабораторной 2 по условию задания. Схема БД
отдельная: `lab3`.

[Задание][assignment]

Все команды выполняются из корня репозитория. Нужны Java 25+ и запущенный
Docker. Maven и GraalVM сборка скачает сама.

## Запуск

```sh
docker compose -f infra/database/compose.yaml up -d --wait
./mvnw -pl lab-3 package
./lab-3/build/java/run.sh
```

На Windows:

```bat
docker compose -f infra/database/compose.yaml up -d --wait
.\mvnw.cmd -pl lab-3 package
.\lab-3\build\java\run.bat
```

В `lab-3/build/java` находятся JAR и скрипты запуска. Сервер слушает
`http://localhost:8080`, остановка через Ctrl+C. Подключение по умолчанию:
`localhost:5432/java_backend_labs`, пользователь и пароль `labs`. Переменные
`DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_SCHEMA` меняют подключение, `PORT` и
`SERVER_ADDRESS` - порт и адрес сервера.

## API

| Метод | Путь | Результат |
| --- | --- | --- |
| POST | `/api/airplanes` | 201, `{"id":1}` и заголовок Location |
| GET | `/api/airplanes` | 200, список по возрастанию ID |
| GET | `/api/airplanes/{id}` | 200, самолет |
| GET | `/api/airplanes/search?model=Airbus%20A320` | 200, первая запись |
| PUT | `/api/airplanes/{id}` | 204, заменить модель и число мест |
| DELETE | `/api/airplanes/{id}` | 204, в том числе при повторном удалении |

Тело POST и PUT: `{"model":"Airbus A320","seats":180}`. Модель обязательна, до
255 символов, без управляющих символов; число мест и ID положительные.
Отсутствующая запись при поиске или обновлении дает 404, некорректный запрос -
400. Сервис бросает собственное `AirplaneNotFoundException`, контроллер ошибок
переводит его в HTTP-ответ. Ошибка БД дает 500 без SQL и параметров подключения
в ответе.

В другом терминале, Linux/macOS:

```sh
curl -i http://localhost:8080/api/airplanes \
  -H 'Content-Type: application/json' \
  -d '{"model":"Airbus A320","seats":180}'
curl http://localhost:8080/api/airplanes
```

В PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/airplanes `
  -Method Post -ContentType application/json `
  -Body '{"model":"Airbus A320","seats":180}'
Invoke-RestMethod http://localhost:8080/api/airplanes
```

## Native

```sh
./mvnw -pl lab-3 -Pnative package
./lab-3/build/native/lab-3
```

На Windows используйте `.\mvnw.cmd` и `.\lab-3\build\native\lab-3.exe`. Сборка
выполняет Spring AOT и создает бинарник для текущей ОС и архитектуры. Для
запуска Java не нужна, для сборки нужен [компилятор C/C++ и SDK][graalvm].

## Проверка

```sh
./mvnw -pl lab-3 verify
./mvnw -pl lab-3 -Pintegration verify
```

Первая команда запускает JUnit/Mockito-тесты всех методов сервиса, MVC-тесты
через `@SpringBootTest` и `MockMvc`, затем Spotless и SpotBugs. База данных
не нужна.
Вторая дополнительно проверяет репозиторий и полный REST CRUD на PostgreSQL в
Testcontainers. Для нее нужен Docker; рабочая база не используется.

[assignment]: https://gitlab.com/VladKarandashov/java-backend-course-demo/-/blob/main/assignments/3-web-tests/README.md
[graalvm]: https://www.graalvm.org/jdk25/reference-manual/native-image/#prerequisites
