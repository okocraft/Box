# Box

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/okocraft/Box)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/okocraft/Box/gradle.yml?branch=main)
![GitHub](https://img.shields.io/github/license/okocraft/Box)

A Paper plugin to provide virtual containers that can store 2.1 billion items per item.

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

- Java 21+
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
    <version>5.3.1</version>
    <scope>provided</scope>
</dependency>
```

```groovy
dependencies {
    compileOnly 'net.okocraft.box:box-api:5.3.1'
}
```

```kotlin
dependencies {
    compileOnly("net.okocraft.box:box-api:5.3.1")
}
```

## License

This project is under the GPL-3.0 license. Please see [LICENSE](LICENSE) for more info.

Copyright © 2019-2024, OKOCRAFT
