<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# httpx plugin Changelog

## [0.3.0]

### Added

- JSON Schema and GraphQL language injection for `application/graphql+json`
- X-GraphQL-Variables introduced for GraphQL variables

```http request
GRAPHQL https://httpbin.org/post
Content-Type: application/graphql
X-GraphQL-Variables:  {"id": 1}

query { user($id: ID) { id name } }
```

## [0.2.0]

### Added

- Memcache: set/get/delete https://httpx.sh/docs/tutorial-basics/memcache-testing
- Redis: rset/hmset/eval https://httpx.sh/docs/tutorial-basics/redis-testing

## [0.1.0]

### Added

- GraphQL: application/graphql with Language injection, HTTP/WebSocket transport
- Pub/Sub for Kafka, Pulsar, RabbitMQ, NATS, Redis, MQTT3/5, AWS SNS/SQN/EventBridge, Aliyun MNS/EventBridge etc
- Email: send email by SMTP
- Cloud OpenAPI: AWS and Aliyun
- Apache Thrift: TJSON support now
- Live templates: graphql, kafka, email
- JSON Schema support:
    * Content-Type: application/cloudevents+json
    * HTTP Headers:  X-JSON-Schema, X-JSON-Type, X-Java-Type
