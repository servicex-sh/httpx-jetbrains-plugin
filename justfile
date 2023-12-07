#!/usr/bin/env just --justfile

# build plugin with changelog
build-plugin:
   rm -rf build/distributions
   rm -rf build/classes
   rm -rf build/resources
   rm -rf build/instrumented
   rm -rf build/tmp
   ./gradlew -x test patchPluginXml buildPlugin

# publish plugin
publish-plugin:
   ./gradlew -x test patchPluginXml buildPlugin publishPlugin

# dependencies
dependencies:
   ./gradlew dependencies > dependencies.txt

# nats pub to subject1
nats-pub:
   nats pub subject1 hello
