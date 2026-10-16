# Лабораторная 1: файлы и коллекции

Вариант 15: собрать уникальные `location/name` из CSV с персонажами Rick and
Morty в `LinkedHashSet<String>` и записать в порядке первого появления.
Дополнительное задание - CRUD персонажей в CSV.

[Задание](https://gitlab.com/VladKarandashov/java-backend-course-demo/-/blob/main/assignments/1-io-and-collections/README.md)
| [Варианты](https://gitlab.com/VladKarandashov/java-backend-course-demo/-/blob/main/assignments/1-io-and-collections/variants.md)

Все команды выполняются из корня репозитория. Нужна Java 17+;
Maven и GraalVM JDK 25 сборка скачает сама.

## JAR

Linux и macOS:

```sh
./mvnw package
./lab-1/build/java/run.sh
```

Windows:

```bat
.\mvnw.cmd package
.\lab-1\build\java\run.bat
```

В `lab-1/build/java` будут JAR, скрипты запуска и `characters.csv`.
Скрипт прочитает этот CSV и сохранит рядом `locations.txt` с 8 локациями.
При переносе папки на другой компьютер для запуска нужна Java 25+.

## Native

Linux и macOS:

```sh
./mvnw -Pnative package
cd lab-1/build/native
./lab-1 -i characters.csv -o locations.txt
```

Windows:

```bat
.\mvnw.cmd -Pnative package
cd lab-1\build\native
.\lab-1.exe -i characters.csv -o locations.txt
```

В `lab-1/build/native` будут бинарник для текущей ОС и архитектуры и копия CSV.
Для запуска Java не нужна. Для сборки нужен системный компилятор C/C++ и SDK:
[требования GraalVM](https://www.graalvm.org/jdk25/reference-manual/native-image/#prerequisites).

## Использование

Свои пути передавайте через `-i` и `-o`; справку покажет `--help`.
Эти аргументы также принимают `run.sh` и `run.bat`.
Флаг `--crud-demo` покажет создание, чтение, изменение и удаление персонажа
во входном CSV. Для экспериментов берите копию: CRUD не поддерживает транзакции
и параллельную запись.

`./mvnw test` запускает тесты, `./mvnw verify` - тесты и SpotBugs.
На Windows используйте `.\mvnw.cmd`.
