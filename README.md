Система управления банковскими картами (Card Management System)

Описание проекта
Backend-система для управления банковскими картами, разработанная на Java с использованием Spring Boot.
 Система предоставляет REST API для создания, управления и просмотра данных о банковских картах, а также выполнения операций между картами пользователя.

Функциональные возможности
Для администраторов:
1.Создание, блокировка, активация и удаление карт
2.Просмотр всех карт в системе
3.Управление пользователями

Для пользователей:
1.Просмотр своих карт с поиском и постраничной выдачей
2.Запрос на блокировку карты
3.Переводы между своими картами
4.Просмотр баланса по картам

Технологии
Java 17+
Spring Boot 3.x
Spring Security + JWT
Spring Data JPA (Hibernate)
PostgreSQL/MySQL
Liquibase (миграции БД)
Docker (развертывание)
OpenAPI (Swagger UI) (документация API)
JUnit 5 (тестирование)
Требования
JDK 17+
Maven 3.8+
Docker 20.10+ (опционально)
PostgreSQL 14+ или MySQL 8+ (если не используется Docker)

Установка и запуск
Вариант 1: Запуск с помощью Docker (рекомендуется)
Клонируйте репозиторий:

bash
git clone https://github.com/your-username/card-management-system.git

cd card-management-system
Запустите приложение с помощью Docker Compose:

bash
docker-compose up -d
Приложение будет доступно по адресу: http://localhost:8080

Вариант 2: Локальный запуск
Создайте базу данных (PostgreSQL или MySQL) и обновите настройки в application.yml

Запустите приложение:

bash
mvn spring-boot:run
Настройка
Основные настройки можно изменить в файле src/main/resources/application.yml:

yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/card_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true

app:
  jwt:
    secret: your-secret-key
    expiration: 86400000 # 24 hours
Документация API
После запуска приложения документация API доступна через Swagger UI:

http://localhost:8080/swagger-ui.html

Также доступна OpenAPI спецификация:

http://localhost:8080/v3/api-docs

Тестирование
Для запуска тестов выполните:

bash
mvn test
Миграции базы данных
Миграции управляются с помощью Liquibase и находятся в src/main/resources/db/changelog/.

Примеры запросов
Аутентификация
http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
Получение списка карт (для пользователя)
http
GET /api/cards
Authorization: Bearer <your-jwt-token>
Создание новой карты (для администратора)
http
POST /api/cards
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "cardNumber": "4111111111111111",
  "cardHolder": "John Doe",
  "expirationDate": "12/25",
  "balance": 1000.00
}
