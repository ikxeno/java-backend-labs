# Java Backend Labs

Лабораторные по [курсу Java Backend][course], вариант 15.

- [Лабораторная 1](lab-1/README.md): файлы, коллекции и CRUD в CSV.
- [Лабораторная 2](lab-2/README.md): JDBC, PostgreSQL, HikariCP и Flyway.
- [Лабораторная 3](lab-3/README.md): Spring Boot, REST API и тесты.

Команды сборки и запуска находятся в папке лабораторной. Версии зависимостей и
SpotBugs и Spotless настроены в корневом `pom.xml`, CI - в
`.github/workflows/github-ci.yaml`.

Java-код форматируется командой `./mvnw spotless:apply` (ширина 80 символов).
`./mvnw verify` проверяет форматирование, тесты и SpotBugs. Длинные импорты
и неделимые значения конфигурации сохраняются целиком.

[course]: https://gitlab.com/VladKarandashov/java-backend-course-demo/-/tree/main/assignments
