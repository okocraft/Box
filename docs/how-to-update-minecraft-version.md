# How to update the Minecraft version

Use this procedure when updating Box to a new Minecraft/Paper version.

## 1. Set the target version

Determine:

- previous and target Minecraft versions;
- a compatible Paper API version;
- the target Minecraft data version.

Update `gradle/libs.versions.toml` if Renovate has not already updated Paper, then run:

```shell
./gradlew build
```

Fix any Paper/Bukkit API incompatibilities before continuing.

## 2. Generate item data

Update `data-generator/build.gradle.kts`:

```kotlin
val previousMinecraftVersion = "<previous>"
val minecraftVersion = "<target>"
```

`previousMinecraftVersion` must have a committed file at:

```text
data-generator/src/main/resources/generated/items/<previous>.txt
```

Run:

```shell
./gradlew :box-data-generator:runServer
```

`runServer` must stop automatically after generation. Its outputs are:

```text
data-generator/build/resources/generated-data/<target>.txt
data-generator/build/resources/generated-data/<target>-new-items.txt
data-generator/build/resources/generated-data/<target>-uncategorized-items.txt
```

If the generator cannot run locally, create a temporary GitHub Actions workflow that runs the same task, verifies these files, and uploads the generated directory as an artifact. Remove the workflow after verification. A timeout is a failure safeguard, not a successful substitute for normal auto-stop.

## 3. Update `MCDataVersion`

Every Minecraft version supported by Box must have a constant in:

```text
api/src/main/java/net/okocraft/box/api/util/MCDataVersion.java
```

Add the target version in chronological order using the data version confirmed from the target runtime or another authoritative source:

```java
public static final MCDataVersion MC_<version> = new MCDataVersion(<data-version>);
```

Also check versions between the previous and target versions and fill any missing constants for versions Box already supports.

For reference, Minecraft 26.2 and 26.3 use 4903 and 5023 respectively. These are examples only; determine the correct data version for every future update.

This step is independent of item renames. Do not update `RenamedItems.VERSIONS` or rename resources unless identifiers actually changed.

## 4. Commit the generated list and handle renames

Copy:

```text
data-generator/build/resources/generated-data/<target>.txt
```

to:

```text
data-generator/src/main/resources/generated/items/<target>.txt
```

Compare the previous and target complete lists. Investigate every removed identifier.

If an identifier was renamed:

1. add `item-provider/src/main/resources/<data-version>.txt` with `OLD_NAME:NEW_NAME` mappings;
2. register the corresponding `MCDataVersion` constant in `RenamedItems.VERSIONS`;
3. update affected category entries using the existing rename syntax;
4. rerun the generator and confirm the renamed item is not reported as new.

Do not modify rename resources or `RenamedItems.VERSIONS` when there are no identifier renames.

## 5. Categorize new items

Use `<target>-new-items.txt` and `<target>-uncategorized-items.txt` to update:

```text
features/category/src/main/resources/default_categories.yml
```

New items must use the target data version:

```yaml
  - <data-version>:<ITEM_NAME>
```

Classify from existing Box categories, similar item families, and official Minecraft change information. If these provide a defensible classification, complete and verify it; ask only when they do not.

Keep each item near related existing entries instead of appending all new items at the category end. Follow the category's existing ordering, for example by wood family, shape/type, base item, or related plants.

If a genuinely new category is needed, also update:

```text
features/category/src/main/java/net/okocraft/box/feature/category/internal/category/defaults/DefaultCategories.java
```

After ordering changes, the set of `(category, data-version, item)` tuples for the new items must be unchanged.

Rerun the generator. `<target>-uncategorized-items.txt` must be empty.

## 6. Verify

Run:

```shell
./gradlew :box-data-generator:runServer
./gradlew build
```

Confirm:

- `runServer` exits normally;
- the uncategorized file is empty;
- the new-items file contains only genuinely new identifiers;
- the generated `<target>.txt` exactly matches the committed file;
- every removed identifier is explained or migrated;
- `MCDataVersion` contains the target and any missing intermediate supported versions;
- rename resources and `RenamedItems.VERSIONS` are updated only when renames exist;
- category ordering did not change item count, category, or data-version guards;
- tests pass and any temporary workflow is removed.
