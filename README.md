httpx plugin for JetBrains IDEs
==============================

<!-- Plugin description -->
**httpx plugin** is a plugin for JetBrains IDE to execute httpx requests in HTTP file.

The following features are available for httpx plugin:

- GraphQL: application/graphql with Language injection(GraphQL plugin required), HTTP/WebSocket transport
- Pub/Sub for Kafka, Pulsar, RabbitMQ, NATS, Redis, MQTT3/5, AWS SNS/SQN/EventBridge, Aliyun MNS/EventBridge etc
- Email: send email by SMTP
- Cloud OpenAPI: AWS and Aliyun
- Apache Thrift: TJSON support now
- Memcache: set/get/delete
- Redis: rset/hmset/eval, and Pub/Sub
- SSH: execute script on remote host
- Live templates: graphql, kafka, email
- JSON Schema support:
    * Content-Type: application/cloudevents+json
    * HTTP Headers:  X-JSON-Schema, X-JSON-Type, X-JSON-Path

How to use?

* GitHub GraphQL API test

```
### GitHub API test
GRAPHQL https://api.github.com/graphql
Authorization: token {{GITHUB_TOKEN}}
Content-Type: application/graphql

query {
    viewer { login }
}
```

* Publish Apache Kafka Message

```
### Publish kafka message
//@name kafka-pub
PUB testTopic
Host: kafka://localhost:9092
Content-Type: application/json

{
  "name": "Jackie"
}
```

For more please visit https://httpx.sh or [httpx cheat sheet](https://cheatography.com/linux-china/cheat-sheets/httpx/)

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

# JSON Schema support

* JSON Schema URL:

```

X-JSON-Schema: https://json.schemastore.org/vsconfig.json
```

* JSON type

```
X-JSON-Type: {name: string, age:number}
X-JSON-Type: [ { name: string, age: number, email?: string }, string]
```

* object: {id:number, name:string}
* tuple: [string, number]
* array: string[]
* set: Set<string>
* normal type: string, object, integer, number, 1..100(range), boolean, date-time, time, date, email, hostname, ipv4, ipv6, uuid, uri

if property name ends with `?`, and it means property not required.

# References

* httpx: https://httpx.sh
* JetBrains HTTP client: https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html
* JSON Schema Store: https://www.schemastore.org/json/






