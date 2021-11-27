# Box GitHub Pages

## Javadocs

- [Latest Release](https://okocraft.github.io/Box/release)
- [Snapshot](https://okocraft.github.io/Box/snapshot)

## Maven Repository

### For release

#### Maven

```xml
<repository>
    <id>okocraft-box-repo</id>
    <url>https://okocraft.github.io/Box/maven/</url>
</repository>
```

#### Gradle (Groovy DSL)

```
repositories {
    maven {
        url 'https://okocraft.github.io/Box/maven/'
    }
}
```

#### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven {
        url = uri("https://okocraft.github.io/Box/maven/")
    }
}
```

### For snapshot

#### Maven

```xml
<repository>
    <id>okocraft-box-snapshot-repo</id>
    <url>https://okocraft.github.io/Box/maven-snapshot/</url>
</repository>
```

#### Gradle (Groovy DSL)

```
repositories {
    maven {
        url 'https://okocraft.github.io/Box/maven-snapshot/'
    }
}
```

#### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven {
        url = uri("https://okocraft.github.io/Box/maven-snapshot/")
    }
}
```

<!-- rendering tool https://til.simonwillison.net/tools/render-markdown -->
