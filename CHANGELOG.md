<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# httpx plugin Changelog


## [0.7.1]

### Fixed

* `MSGPACK` fix for Neovim RPC test
* `X-Args-0`, `X-Args-0`  introduced for `MSGPACK` with Language injection

```
### msgpack request
MSGPACK 127.0.0.1:6666/nvim_exec_lua
X-Args-1: []
Content-Type: text/x-lua

return vim.api.nvim_win_get_cursor(0)[1]
```

## [0.7.0]

### Added
          
- Lua Language injection for `EVAL` method 
- Multi subjects support for Nats:  `SUB subject1,subject2` or `PUB subject1,subject2`
- tarpc support: https://github.com/google/tarpc

```
### tarpc request
TARPC 127.0.0.1:4500/hello
Content-Type: application/json
     
{
  "name": "jackie"
}
```
- msgpack-rpc support: https://github.com/msgpack-rpc/msgpack-rpc

```
### msgpack request
MSGPACK 127.0.0.1:18800/add
Content-Type: application/json

[1, 2]
```

## [0.6.1]

### Added

- Intention Action: convert to ALIYUN request to CLI
- Past Processor:  convert aliyun cli to ALIYUN request in http file

## [0.6.0]

### Added

- Aliyun: code completion for host, param and json body

## [0.5.0]

### Added

- AWS support: AWS, AWSPUT, AWSPOST, AWSDELETE

## [0.4.0]

### Added

- SSH support: https://httpx.sh/docs/tutorial-basics/ssh-testing

```http request
### ssh with user name and password
SSH username:password@remote_host

cd /
ls -al

### ssh with known host
SSH ubuntu@known_host

ls -al

### ssh with private key
SSH root@host.example.com
X-SSH-Private-Key: /path-to-private-key

ls -al
```

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
