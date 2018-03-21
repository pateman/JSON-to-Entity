# JSON to Entity

A Hybris-like entity generator, which could be used to generate database entity classes from a simple JSON definition. The code is fully documented and there are a few unit tests for you to have a look and learn how it works. The code is mostly finished, but I am leaving it here for someone to pick it up and adapt to their needs. The code is just an example and it could be extended however you like. 

Currently it supports generating entities and relations between them. Freemarker is used for the actual source code generation, so you can easily customize the template (the default one is called `defaultEntityTemplate.ftlh`).

Oh, there is also a handy Gradle task for running the actual generation process. ;)

### Example of JSON schema
```{
  "entities": [
    {
      "name": "User",
      "deployment": "users",
      "classFile": "pl.pateman.entitygenerator.entity.UserEntity",
      "attributes": [
        {
          "name": "id",
          "type": "java.lang.Long",
          "flags": [
            "PRIMARY_KEY"
          ]
        },
        {
          "name": "username",
          "type": "java.lang.String"
        },
        {
          "name": "password",
          "type": "java.lang.String"
        }
      ]
    },
    {
      "name": "Customer",
      "deployment": "customers",
      "classFile": "pl.pateman.entitygenerator.entity.CustomerEntity",
      "root": {
        "name": "User",
        "extend": false
      },
      "attributes": [
        {
          "name": "username",
          "type": "java.lang.String",
          "reintroduce": true,
          "flags": [
            "UNIQUE"
          ]
        },
        {
          "name": "firstName",
          "type": "java.lang.String"
        },
        {
          "name": "lastName",
          "type": "java.lang.String"
        }
      ]
    }
    ...
  ],
  "relations": [
    {
      "source": {
        "entity": "Customer",
        "attributeName": "orders",
        "side": "ONE",
        "collectionType": "LIST"
      },
      "target": {
        "entity": "Order",
        "attributeName": "customer",
        "side": "MANY"
      },
      "joinColumn": "custId"
    }
    ...
  ]
}
```

### Usage

Have a look at `pl.pateman.entitygenerator.EntityGeneratorTest` to learn how to use the generator. There is also a Gradle task called `generateEntities` which runs an executor to generate sources.

### Third-party code and libraries
* [GSON](https://github.com/google/gson) - for parsing JSON schema
* [Reflections](https://github.com/ronmamo/reflections) - for scanning the classpath for available JSON schemas
* [Apache Commons Lang3](https://commons.apache.org/proper/commons-lang/) - various utility methods used throughout the whole code
* [FreeMarker](https://freemarker.apache.org/) - for generating the actual source code files