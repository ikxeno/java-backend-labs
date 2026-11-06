# Лабораторная 2: база данных

Вариант 15: `Airplane` с полями `id`, `model`, `seats`. JDBC-репозиторий
выполняет CRUD, HikariCP управляет соединениями, Flyway создает таблицу и
применяет миграции при запуске.

[Задание][assignment]
| [Варианты][variants]

Все команды выполняются из корня репозитория. Нужны Java 25+ и запущенный
Docker. Maven и GraalVM сборка скачает сама.

## Запуск

```sh
docker compose -f infra/database/compose.yaml up -d --wait
./mvnw -pl lab-2 package
./lab-2/build/java/run.sh
```

На Windows:

```bat
docker compose -f infra/database/compose.yaml up -d --wait
.\mvnw.cmd -pl lab-2 package
.\lab-2\build\java\run.bat
```

В `lab-2/build/java` находятся JAR и скрипты запуска. Программа показывает
создание, поиск по ID и модели, список, изменение и удаление самолета. При
успешном завершении демонстрационная запись удаляется. Повторный запуск
сохраняет чужие записи.

## Native

```sh
./mvnw -pl lab-2 -Pnative package
./lab-2/build/native/lab-2
```

На Windows используйте `.\mvnw.cmd` и `.\lab-2\build\native\lab-2.exe`. Бинарник
собирается для текущей ОС и архитектуры, для его запуска Java не нужна. Для
сборки нужен [компилятор C/C++ и SDK][graalvm].

## База данных

По умолчанию: `localhost:5432/java_backend_labs`, пользователь и пароль `labs`,
схема `lab2`. Настройки в `lab-2/src/main/resources/database.properties`;
переменные `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_SCHEMA` переопределяют их.
Можно использовать установленный PostgreSQL вместо Docker.

Модель содержит от 1 до 255 символов, без управляющих символов, и не может
состоять из пробелов; число мест положительное. ID выдает БД. При одинаковых
моделях `findByModel` возвращает запись с наименьшим ID. Отсутствующая запись
дает `null` при поиске и `false` при обновлении. Повторное удаление безопасно.
Все операции, кроме создания, идемпотентны.

Каждая запись выполняется одним SQL-запросом в транзакции PostgreSQL. Вся
демонстрация не является одной общей транзакцией: при ошибке уже выполненные
операции сохраняются. Данные Docker хранятся в томе. Остановить базу:

```sh
docker compose -f infra/database/compose.yaml down
```

## Проверка

```sh
./mvnw -pl lab-2 -Pintegration verify
```

Testcontainers поднимает отдельный PostgreSQL: проверяются CRUD, повторные
операции, миграции, ограничения данных и SQL-параметры. Затем запускаются
Spotless и SpotBugs. Без Docker можно выполнить `./mvnw -pl lab-2 verify`:
сборка, модульные тесты и статические проверки.

[assignment]: https://gitlab.com/VladKarandashov/java-backend-course-demo/-/blob/main/assignments/2-database/README.md
[variants]: https://gitlab.com/VladKarandashov/java-backend-course-demo/-/blob/main/assignments/2-database/variants.md
[graalvm]: https://www.graalvm.org/jdk25/reference-manual/native-image/#prerequisites
