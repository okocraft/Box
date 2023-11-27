# Box

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/okocraft/Box)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/okocraft/Box/gradle.yml?branch=main)
![GitHub](https://img.shields.io/github/license/okocraft/Box)

Box is a plugin for Paper that allows users to store any items in a virtual inventory.

<!--- TODO English
## 特徴

Box には以下のような特徴があります。

* GUI によって直感的にアイテムの預け入れ・引き出し・クラフトができます
* コマンドからの預け入れ・引き出しもサポートします
* Box Stick (`/box stick`) を使うことで、アイテムを Box から消費することができます
* 通常アイテム、ポーション、花火、エンチャント本はデフォルトで収納できます
* `/boxadmin register` で手に持っているアイテムを Box に登録することができます
* 詳細な使い方は [Wiki](https://github.com/okocraft/Box/wiki) で確認することができます
--->

## Requirements

- Java 17+
- Paper 1.17+

## Installation

Please read [Installation and Setup](https://github.com/okocraft/Box/wiki/Installation-and-Setup) page on GitHub Wiki.

## Compiling from source

```shell
git clone https://github.com/okocraft/Box.git
cd Box/
./gradlew build
```

You can find the bundled jar in the `bundle/build/libs` directory.


## API

### Javadocs

- [Latest Release](https://okocraft.github.io/Box/release)
- [Snapshot](https://okocraft.github.io/Box/snapshot)

### Usage

#### 1. Add a repository

```xml
<repository>
    <id>okocraft-box-repo</id>
    <url>https://okocraft.github.io/Box/maven/</url>
</repository>
```

```groovy
repositories {
    maven {
        url 'https://okocraft.github.io/Box/maven/'
    }
}
```

```kotlin
repositories {
    maven {
        url = uri("https://okocraft.github.io/Box/maven/")
    }
}
```

For snapshot version, use https://okocraft.github.io/Box/maven-snapshot/

#### 2. Add to dependencies

```xml
<dependency>
    <groupId>net.okocraft.box</groupId>
    <artifactId>box-api</artifactId>
    <version>5.5.1</version>
    <scope>provided</scope>
</dependency>
```

```groovy
dependencies {
    compileOnly 'net.okocraft.box:box-api:5.5.1'
}
```

```kotlin
dependencies {
    compileOnly("net.okocraft.box:box-api:5.5.1")
}
```

#### 3. Relocate ConfigAPI and Event4J

If you are using Box events (`BoxAPI#getEventBus`) or configurations (`BoxAPI#getConfiguration` or using ConfigAPI directly), you have to relocate them.

Add `maven-shade-plugin` or [Gradle Shadow](https://github.com/johnrengelman/shadow) to your project and write the following setting:

```xml
<relocations>
  <relocation>
    <pattern>com.github.siroshun09.configapi</pattern>
    <shadedPattern>net.okocraft.box.lib.configapi</shadedPattern>
  </relocation>
  <relocation>
    <pattern>com.github.siroshun09.event4j</pattern>
    <shadedPattern>net.okocraft.box.lib.event4j</shadedPattern>
  </relocation>
</relocations>
```

```groovy
shadowJar {
  relocate 'com.github.siroshun09.configapi', 'net.okocraft.box.lib.configapi'
  relocate 'com.github.siroshun09.event4j', 'net.okocraft.box.lib.event4j'
}
```

```kotlin
shadowJar {
  relocate("com.github.siroshun09.configapi", "net.okocraft.box.lib.configapi")
  relocate("com.github.siroshun09.event4j", "net.okocraft.box.lib.event4j")
}
```

## License

This project is under the GPL-3.0 license. Please see [LICENSE](LICENSE) for more info.

Copyright © 2019-2023, OKOCRAFT
