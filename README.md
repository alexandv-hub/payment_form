# Описание задачи

Необходимо создать веб-приложение на основе Spring Boot, которое предоставляет интерфейс для ввода суммы и выбора валюты, а затем отправляет запрос на оплату через API сервиса [PayTech](https://paytech.readme.io/). Пользователь взаимодействует с простой веб-формой, где вводит необходимые данные и нажимает кнопку "Оплатить".

После отправки формы происходит вызов метода API POST `/api/v1/payments` с параметрами `paymentType=DEPOSIT`, `paymentMethod=BASIC_CARD`, а также с введенными пользователем валютой и суммой. В случае успешной обработки платежа пользователь перенаправляется на URL, указанный в ответе. В случае ошибки отображается соответствующая страница с информацией об ошибке.
_____
### Технологии

Java17, PostgreSQL, Spring (Boot, Data, MVC), Thymeleaf, Mapstruct, Flyway, Maven, Docker, JUnit, Mockito, Testcontainers.
_____
### Конфигурация

Для работы с API PayTech используется Bearer токен, который должен быть указан в конфигурации приложения.

Пример конфигурационного файла `application.yaml`:

```yaml
spring:
  application:
    name: payment_app

app:
  paytech:
    api-url: https://api.sandbox.paytech.com/v1/payments
    bearer-token: <ваш Bearer токен>
```
_____

### Запуск приложения
**Шаг 1: Генерация SSL сертификата**
- Для работы приложения с HTTPS необходимо создать SSL сертификат в формате keystore.p12. Выполните следующую команду:
```bash
keytool -genkeypair -alias yourkeyalias -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src/main/resources/ssl/keystore.p12 -validity 3650
```
-	alias yourkeyalias — имя ключа, используемое для доступа к сертификату.
-	key-store-password — пароль, который вы укажете при генерации, должен совпадать с параметром key-store-password в application.yml:
```yaml
server:
  port: 443
  ssl:
    key-store: keystore.p12
    key-store-password: yourkeystorepassword
    keyStoreType: PKCS12
    keyAlias: yourkeyalias
```
_____


**Шаг 2: Сборка проекта с использованием Maven**
```bash
mvn clean package
```
_____

**Шаг 3: Запуск контейнеров**
- Для запуска приложения и базы данных выполните следующую команду:
```bash
docker compose up -d
```
_____

**Шаг 4: Проверка работы приложения**

После успешного запуска приложение будет доступно по адресу:

https://localhost:443/api/v1/payments/form
_____
