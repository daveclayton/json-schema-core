## Travis builds now enabled

Builds are now verified by Travis (see [issue #20](https://github.com/daveclayton/json-patch/issues/20) from the json-patch project for details)

https://travis-ci.org/daveclayton/json-schema-core

## Read me first

The license of this project is dual licensed LGPLv3 or later/ASL 2.0. See file `LICENSE` for more
details. The full text of both licensed is included in the package.

## What this is

This package contains the core mechanics of [json-schema-validator
library](https://github.com/daveclayton/json-schema-validator). It also provides a comprehensive
infrastructure to build processing chains for anything you can think of, really. To this effect,
this package can be used, for instance, to perform the following, provided you use the appropriate
software packages:

* generate a JSON Schema from a POJO, and then validate instances against that schema;
* transform different, related schema formats into JSON Schema, or the reverse (for instance Avro);
* conditional patching/deserialization;
* etc etc.

You can see sample usages of this library in a [separate
project](https://github.com/daveclayton/json-schema-processor-examples) which is [demonstrated
online](http://json-schema-validator.herokuapp.com). More details on this library can
be found [here](https://github.com/daveclayton/json-schema-core/wiki/Architecture).


## Versions

The current stable verson is **1.2.5**
([ChangeLog](https://github.com/daveclayton/json-schema-core/wiki/ChangeLog_12x),
[Javadoc](http://daveclayton.github.io/json-schema-core/1.2.x/index.html)).

The old verson is **1.0.4**
([ChangeLog](https://github.com/daveclayton/json-schema-core/wiki/ChangeLog_10x),
[Javadoc](http://daveclayton.github.io/json-schema-core/1.0.x/index.html)).

See [here](https://github.com/daveclayton/json-schema-core/wiki/Whatsnew_12) for
the major changes between 1.0.x and 1.2.x.

## Using this project with gradle/maven

For gradle, use:

```
dependencies {
    compile(group: "com.github.fge", name: "json-shema-core", version: "1.2.3");
}
```

For maven:

```xml
<dependency>
    <groupId>com.github.fge</groupId>
    <artifactId>json-schema-core</artifactId>
    <version>1.2.3</version>
</dependency>
```

You can also get the jars from [Bintray](https://bintray.com/fge/maven/json-schema-core).

## Versioning scheme policy

The versioning scheme is defined by the **middle digit** of the version number:

* if this number is **even**, then this is the **stable** version; no new features will be
  added to such versions, and the user API will not change (save for some additions if requested).
* if this number is **odd**, then this is the **development** version; new features will be
  added to those versions only, **and the user API may change**.

