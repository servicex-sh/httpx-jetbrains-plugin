httpx plugin for JetBrains IDEs
==============================

<!-- Plugin description -->
**httpx plugin** is a plugin for JetBrains IDE to execute httpx requests in HTTP file.

The following features are available for Dubbo:

* GraphQL Request
* Email
* Pub/Sub - Kafka, RabbitMQ, Nats, Redis, RocketMQ, MQTT
* Code completion for CloudEvents json format

<!-- Plugin description end -->

# Pub/Sub message

```
### send kafka message
//@name kafka-pub
PUB testTopic
Host: kafka://localhost:9092
Content-Type: application/json

{
  "name": "Jackie"
}
```

* Apache Kafka: `kafka://localhost:9092/`
* Apache Pulsar: `pulsar://localhost:6650`
* Apache RocketMQ: `rocketmq://localhost:9876`
* RabbitMQ: `amqp://localhost:5672`
* Nats:  `nats://localhost:4222`
* MQTT: `mqtt://localhost:1883`
* Redis Channel: `redis://localhost:6379`

# Send email

```
### send an email
//@name gmail-send
MAIL mailto:demo@example.com
Host: ssl://smtp.gmail.com:465
Authorization: Basic yourname@gmail.com:xxxx
From: yourname@gmail.com
Subject: e-nice to meet you
Content-Type: text/plain
                
Hi Jerry:
  this is testing email.
                
Best regards
Tom
```

# GraphQL over WebSocket

```
### graphql query over WebSocket
//@name subscription
GRAPHQLWS localhost:4000/graphql
Content-Type: application/graphql

subscription { greetings }
```

# References

* httpx: https://httpx.sh
* JetBrains HTTP client: https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html





