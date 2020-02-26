# Сервис поиска заказов (из elastic).

## API
1. POST /orders - получение списка заказов по фильтру (передается в теле).
Коды ошибок:
200: Успешный запрос (+ массив заказов)
400: Отсутсвует обязательный параметр. Обязательным параметром является только заголовок Authorization.
403: Доступ запрещен. Если в access_token нету необходимого permmision на данный метод и сервис. Наличие в RPT scope 'read' для rsname 'api-orders'.
500: Системное исключение. Все остальные неописанные исключения.
509: Превышен лимит времени на запрос. Во внешнюю систему. В контексте данного метода, в эластик.
2. GET /order/:id получение карточки заказа.

## Доп. информация

### Cassandra
Keyspace=orders_service
Настройки в `application.conf`. Адрес Cassandra задается
```hocon
cassandra.default.contact-points = ["host1:port1","host2:port2"]
```
[Application.conf: cassandra](impl/src/main/resources/application.conf#Cassandra "Перейти к секции cassandra в конфигурации application.conf")

### Kafka
Адрес Kafka брокера задается в конфигурации:
```hocon
lagom.broker.kafka.brokers = "host1:port1,host2:port2"
```

#### Elastic Search
Адрес задается в конфигурации. `elastic-search` для поиска, `elastic-document` для индексации.
```hocon
akka {
    config {
      services {
        elastic-search {
          endpoints = [
            {
              host = ${ELASTIC_SEARCH_HOST}
              port = ${ELASTIC_SEARCH_PORT}
            }
          ]
        }
        elastic-document {
          endpoints = [
            {
              host = ${ELASTIC_SEARCH_HOST}
              port = ${ELASTIC_SEARCH_PORT}
            }
          ]
        }
      }
    }
  }
}
```
[Application.conf: ES](impl/src/main/resources/application.conf#ES "Перейти к секции elastic search в конфигурации application.conf")

### БД
```hocon
db.default {
  url = ${DB_URL}
  # или url = "jdbc:oracle:thin:@//todo_test_server-db-X:Y521/ORCLPDB1"
  username = "foo"
  password = "bar"
}
```
здесь `DB_URL` переменная окружения, задающая строку подключения к БД, например, `jdbc:oracle:thin:@//todo_test_server-db-X:Y521/ORCLPDB1`.
Для Helm чарта значение переменной задается через переменную в CI-скрипте гитлаба.
[Application.conf: JPA](impl/src/main/resources/application.conf#JPA "Перейти к секции JPA в конфигурации application.conf")

#### Values.yaml
Переменные окружения задаются при инсталлировании и обновлении приложения (сервиса).
Список хостов и шаблонов доменных имен, доступ к которым должен быть в обход прокси,
может быть настроен для чарта Helm через переменную `nonProxyHosts`, объявленную в `values.yml`.

```
"localhost|*.todo_inner_net.ru"
```
В `values.yaml` запрещено указывать переменные для доступа к тестовым сервисам.


## Тестирование

### Настройка базы данных

Перед началом тестирования необходимо создать пользователя в БД Oracle.
Вариант подключения к БД стенда через Putty:
```docker exec -it -u:root --privileged testY_db_1 sqlplus sys/Welcome1@TODO_DB_SID as sysdba
```
Необходимо под пользователем SYS выполнить следующие команды:
```sql
COMMIT;
ALTER SESSION SET CONTAINER = ORCLPDB1;
CREATE USER orders_service IDENTIFIED BY Welcome1 CONTAINER=CURRENT;
GRANT ALL PRIVILEGES TO orders_service;
COMMIT;
-- Если хочется вернуться, то обратно меняем контейнер
ALTER SESSION SET CONTAINER = CDB$ROOT;
-- Проверить текущий контейнер
SELECT SYS_CONTEXT('USERENV', 'CON_NAME') NAME FROM DUAL;
```
### Индекс ES
Перед первым использованием создать индекс в elastic search:
Метод [PUT] /order
[Тело сообщения: json индекса.](/gitlab-ci/order_index.json)

### Настройка доступа к кафке в кубернетисе
Config map в кубернетесе. Ключ conf. Параметр lagom.broker.kafka.brokers.

### Пример сообщения кафки
_Обратить внимание на формат даты и чисел._
```
{
  "type": "order",
  "id": 27324,
  "cancelReason": "cancelreason27324",
  "city": "city27324",
  "clientEmail": "27324",
  "clientName": "clientname27324",
  "clientPhone": "27324",
  "comment": "27324",
  "creationDate": "2003-06-01T04:22:40+04:00",
  "mnp": 27324,
  "network": "network27324",
  "orderComposition": [
    {
      "item": {
        "commerceId": "ordercomposition_comm_id_1",
        "type": "27324",
        "frontName": "27324",
        "mnp": "89770000001",
        "amount": 3,
        "price": {
          "amount": 100,
          "currency": "RUB"
        },
        "salePrice": {
          "amount": 99.99,
          "currency": "RUB"
        }
      },
      "subItem": {
        "commerceId": "ordercomposition_sub_comm_id_1",
        "frontName": "ordercomposition_sub_front_1",
        "amount": 2
      }
    },
    {
      "item": {
        "commerceId": "ordercomposition_comm_id_1",
        "type": "27324",
        "frontName": "27324",
        "mnp": "89770000001",
        "amount": 3,
        "price": {
          "amount": 100,
          "currency": "RUB"
        },
        "salePrice": {
          "amount": 99.99,
          "currency": "RUB"
        }
      },
      "subItem": {
        "commerceId": "ordercomposition_sub_comm_id_1",
        "frontName": "ordercomposition_sub_front_1",
        "amount": 2
      }
    },
    {
      "item": {
        "commerceId": "ordercomposition_comm_id_1",
        "type": "27324",
        "frontName": "27324",
        "mnp": "8-977-000-00-01",
        "amount": 3,
        "price": {
          "amount": 100,
          "currency": "RUB"
        },
        "salePrice": {
          "amount": 99.99,
          "currency": "RUB"
        }
      },
      "subItem": {
        "commerceId": "ordercomposition_sub_comm_id_1",
        "frontName": "ordercomposition_sub_front_1",
        "amount": 2
      }
    },
    {
      "item": {
        "commerceId": "ordercomposition_comm_id_1",
        "type": "27324",
        "frontName": "27324",
        "mnp": "89770000001",
        "amount": 3,
        "price": {
          "amount": 100,
          "currency": "RUB"
        },
        "salePrice": {
          "amount": 99.99,
          "currency": "RUB"
        }
      },
      "subItem": {
        "commerceId": "ordercomposition_sub_comm_id_1",
        "frontName": "ordercomposition_sub_front_1",
        "amount": 2
      }
    },
    {
      "item": {
        "commerceId": "ordercomposition_comm_id_1",
        "type": "27324",
        "frontName": "27324",
        "mnp": "89770000001",
        "amount": 3,
        "price": {
          "amount": 100,
          "currency": "RUB"
        },
        "salePrice": {
          "amount": 99.99,
          "currency": "RUB"
        }
      },
      "subItem": {
        "commerceId": "ordercomposition_sub_comm_id_1",
        "frontName": "ordercomposition_sub_front_1",
        "amount": 2
      }
    }
  ],
  "orderState": "27324",
  "orderType": "ordertype27324",
  "parentNetwork": "parentnetwork27324",
  "partner": "partner27324",
  "payState": "paystate27324",
  "payType": "paytype27324",
  "promo": "27324",
  "region": "region27324",
  "salePointAddress": "salepointaddress27324",
  "salePointId": "salepoint27324",
  "shipGroupType": 27324,
  "totalPrice": 27324,
  "trackNumber": "tracknumber27324"
}
```
[Elastic search index json](gitlab-ci/order_index.json "Go to index description")
