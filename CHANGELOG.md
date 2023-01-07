<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# httpx plugin Changelog

## [0.17.0]

### Added
                    
- Update dependencies
- TRPC Support

```
### trpc query
#@name trpc-query
TRPC http://localhost:2022/greeting.hello
Content-Type: application/json

{
  "name": "world"
}

### trpc mutate
#@name trpc-mutate
TRPCM http://localhost:2022/post.createPost
Content-Type: application/json

{
  "title": "hello world",
  "text": "check out https://tRPC.io"
}
```

## [0.16.0]

### Added
                    
- Update dependencies
- Redis JSON Support

```
### Redis json set
JSONSET user.1/$
Host: localhost:16379
Content-Type: application/json

{
  "id": 1,
  "name": "jackie",
  "age": 42
}

### Redis json get
JSONGET user.1/$
Host: localhost:16379
```

## [0.15.0]

### Added

- Compatible with JetBrains IDE 2022.3 EAP 4

## [0.14.0]

### Added

- MicroService annotator support for @HttpRequestName

## [0.13.0]

### Added

- Compatible with JetBrains IDE 2022.3

## [0.12.0]

### Added

- Add [RocketMQ](https://rocketmq.apache.org/) support

```
### publish rocketmq message
//@name rocketmq-pub
PUB testTopic
URI: rocketmq://localhost:9876
Content-Type: application/json

{
  "name": "Jackie"
}

### consume rocketmq message
//@name rocketmq-sub
SUB testTopic
URI: rocketmq://localhost:9876
```

## [0.11.0]

### Added

- Compatible with JetBrains IDEs 2022.2
- Disable GRAPHQL method because of built-in by JetBrains
- Dependencies updated

## [0.10.1]

### Added

- Compatible with JetBrains IDEs 2022.*

## [0.10.0]

### Added

- Add 'X-msg-header' custom message headers for PUB method

```
### send pulsar message
PUB test-topic
Host: pulsar://localhost:6650
Content-Type: application/json
X-Custom-Header: header_value

{
  "name": "Jackie"
}
```

## [0.9.2]

### Optimized

- Load private keys from $HOME/.ssh for SSH method

## [0.9.1]

### Added

- Add LOAD method for Redis 7.0 functions: https://redis.io/docs/manual/programmability/functions-intro/

```
### Redis 7.0 functions
LOAD mylib
Content-Type: text/x-lua
  
#!lua name=mylib  
redis.register_function(
  'knockknock',
  function() return 'Who\'s there?' end
)
```

## [0.9.0]

### Added

- REST method added to make language injection easy for JSON Array and Object: https://servicex.sh/docs/tutorial-basics/http-testing

```
### registering a new schema to Spring Cloud Schema Registry Server
REST http://localhost:8990/
X-Args-subject: subject1
X-Args-format: avro
X-Body-Name: definition
Content-Type: text/avsc

{
  "type": "record",
  "namespace": "com.example.messages.avro",
  "name": "Subject1",
  "fields": [
    {
      "name": "name",
      "type": "string"
    }
  ]
}  
```

## [0.8.1]

### Added

- NVIM support to execute nvim api

```
### neovim request
NVIM nvim_exec_lua
Content-Type: text/x-lua

vim.api.nvim_command('!ls')
```

## [0.8.0]

### Added

- `JSONRPC` support

```
### json-rpc over http
JSONRPC http://127.0.0.1:8080/add
Content-Type: application/json

[ 1, 2 ]

### json-rpc over tcp
JSONRPC 127.0.0.1:9080/add
Content-Type: application/json

[ 1, 2 ]
```

## [0.7.1]

### Added

- `X-Args-0`, `X-Args-0`  introduced for `MSGPACK` with Language injection

```
### msgpack request
MSGPACK 127.0.0.1:6666/nvim_exec_lua
X-Args-1: []
Content-Type: text/x-lua

return vim.api.nvim_win_get_cursor(0)[1]
```

### Fixed

- `MSGPACK` fix for Neovim RPC test

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
