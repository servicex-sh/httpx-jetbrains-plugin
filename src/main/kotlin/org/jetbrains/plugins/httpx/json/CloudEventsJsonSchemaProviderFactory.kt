package org.jetbrains.plugins.httpx.json

@Suppress("UnstableApiUsage")
class CloudEventsJsonSchemaProviderFactory : ContentTypeAwareJsonSchemaFileProvider(
    "application/cloudevents+json", "/cloudevents-schema.json"
) {

}