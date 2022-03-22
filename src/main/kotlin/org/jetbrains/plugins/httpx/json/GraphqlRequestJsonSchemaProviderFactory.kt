package org.jetbrains.plugins.httpx.json

@Suppress("UnstableApiUsage")
class GraphqlRequestJsonSchemaProviderFactory : ContentTypeAwareJsonSchemaFileProvider(
    "application/graphql+json", "/graphql-request-schema.json"
) {

}