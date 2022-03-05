#!/usr/bin/env just --justfile

# build plugin with changelog
build-plugin:
   ./gradlew -x test patchPluginXml buildPlugin

# publish plugin
publish-plugin:
   ./gradlew -x test patchPluginXml buildPlugin publishPlugin

# nats pub to subject1
nats-pub:
   nats pub subject1 hello