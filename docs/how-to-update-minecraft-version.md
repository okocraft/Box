# How to update the Minecraft version

This document describes the repeatable workflow for updating Box to a new Minecraft/Paper version.

The procedure is based on previous Minecraft update work, especially:

- [#490 - Minecraft 1.21.7](https://github.com/okocraft/Box/pull/490)
- [#525 - Minecraft 1.21.9 / 1.21.10](https://github.com/okocraft/Box/pull/525)
- [#564 - Minecraft 26.1](https://github.com/okocraft/Box/pull/564)
- [Minecraft 26.2 update commit](https://github.com/okocraft/Box/commit/b6b433951d78557caa558439bbffa3291b578a8a)

Older PRs contain version-specific module changes that are no longer required after the versioning refactor in June 2026. Follow the current repository structure described below.

## Inputs

Before starting, determine:

- the previous Minecraft version currently supported by Box;
- the target Minecraft version;
- a Paper API build compatible with the target version;
- the target Minecraft data version if item rename mappings are required.

Prefer processing released Minecraft versions sequentially when several versions were skipped. This makes item additions and renames easier to review and preserves migration mappings at the correct data-version boundary.

## 1. Update the Paper dependency

Check `gradle/libs.versions.toml`.

Renovate normally updates `io.papermc.paper:paper-api`, so this may already be done. If not, update `paper` to a build for the target Minecraft version.

Do not change unrelated dependencies as part of the Minecraft update. Update `paper-javadoc` only when a matching version is available and the project needs it; it does not have to move in the same commit as `paper`.

Then run:

```shell
./gradlew build
```

Fix compilation errors caused by Paper/Bukkit API changes before continuing. Previous updates required source changes outside the version/data files, so compiler errors must be treated as part of the update rather than bypassed.

## 2. Configure the data generator

Edit `data-generator/build.gradle.kts`:

```kotlin
val previousMinecraftVersion = "<previous>"
val minecraftVersion = "<target>"
```

`previousMinecraftVersion` must correspond to a file already present in:

```text
data-generator/src/main/resources/generated/items/<previous>.txt
```

The data generator starts a Paper server for `minecraftVersion` and writes generated files under:

```text
data-generator/build/resources/generated-data/
```

Run it with:

```shell
./gradlew :box-data-generator:runServer
```

The relevant outputs are:

```text
<target>.txt
<target>-new-items.txt
<target>-uncategorized-items.txt
```

- `<target>.txt` is the complete generated default-item list.
- `<target>-new-items.txt` is the difference from the previous version after known renames are applied.
- `<target>-uncategorized-items.txt` contains default items that are not covered by the bundled category definitions.

## 3. Commit the generated item list

Copy:

```text
data-generator/build/resources/generated-data/<target>.txt
```

to:

```text
data-generator/src/main/resources/generated/items/<target>.txt
```

The `-new-items.txt` and `-uncategorized-items.txt` files are review aids and are not normally committed.

## 4. Categorize new items

Review both diagnostic files.

For each new item, add it to the appropriate category in:

```text
features/category/src/main/resources/default_categories.yml
```

Items introduced in the target version must be guarded by their Minecraft data version:

```yaml
  - <data-version>:<ITEM_NAME>
```

If an item replaces an older name, the category entry can use the rename syntax already used in this file, for example:

```yaml
  - OLD_NAME;<data-version>:NEW_NAME
```

If the new Minecraft release introduces a group that deserves its own default category, also add that category to:

```text
features/category/src/main/java/net/okocraft/box/feature/category/internal/category/defaults/DefaultCategories.java
```

The Minecraft 26.2 update is an example: it added the `sulfur-caves` category in both the Java category list and `default_categories.yml`.

Items that should never be offered as normal Box items, such as internal/test-only items, should be placed in the existing `unavailable` category rather than left uncategorized.

After editing the categories, rerun:

```shell
./gradlew :box-data-generator:runServer
```

The goal is for `<target>-uncategorized-items.txt` to be empty.

## 5. Handle renamed items

Only do this when Minecraft renamed an item identifier.

Add a resource file named after the Minecraft data version:

```text
item-provider/src/main/resources/<data-version>.txt
```

Each mapping is:

```text
OLD_NAME:NEW_NAME
```

For example, Minecraft 1.21.9 used:

```text
CHAIN:IRON_CHAIN
```

Then:

1. add a corresponding `MCDataVersion` constant in
   `api/src/main/java/net/okocraft/box/api/util/MCDataVersion.java`;
2. add that constant to `RenamedItems.VERSIONS` in
   `item-provider/src/main/java/net/okocraft/box/item/RenamedItems.java`;
3. rerun the data generator so `<target>-new-items.txt` does not incorrectly report renamed items as newly added items;
4. update category entries to preserve the old name before the rename and the new name from the rename data version onward.

Do not add an `MCDataVersion` constant merely because Minecraft released a new version. Since the June 2026 refactor, constants are only needed when code needs to refer to that exact data-version boundary, such as an item rename migration.

## 6. Review API-specific breakage

Minecraft updates can require source changes unrelated to generated items. Typical signals are compilation failures or behavior changes in Paper/Bukkit APIs.

Examples from previous updates include:

- changed Adventure/Paper component APIs;
- changed sound/category APIs;
- changed item metadata APIs;
- behavior changes around projectiles or item types.

Keep these fixes in the same Minecraft update only when they are required for the target version.

## 7. Verify

Run the following checks after the generated data and categories are complete:

```shell
./gradlew :box-data-generator:runServer
./gradlew build
```

Verify that:

- `<target>-uncategorized-items.txt` is empty;
- `<target>-new-items.txt` contains only genuinely new items;
- `data-generator/src/main/resources/generated/items/<target>.txt` matches the newly generated full item list;
- every rename has a migration resource and is registered in `RenamedItems.VERSIONS`;
- the project compiles and tests pass;
- any Paper/Bukkit API breakage has been handled intentionally.

## Files normally changed

For a straightforward update after the current versioning refactor, expect changes to a subset of:

```text
gradle/libs.versions.toml
data-generator/build.gradle.kts
data-generator/src/main/resources/generated/items/<target>.txt
features/category/src/main/resources/default_categories.yml
features/category/src/main/java/net/okocraft/box/feature/category/internal/category/defaults/DefaultCategories.java
```

When item identifiers are renamed, also expect:

```text
api/src/main/java/net/okocraft/box/api/util/MCDataVersion.java
item-provider/src/main/java/net/okocraft/box/item/RenamedItems.java
item-provider/src/main/resources/<data-version>.txt
```

Other source files should only change when the new Paper/Bukkit API requires compatibility fixes.

## Automation boundary

The following parts are suitable for automation:

1. update `previousMinecraftVersion` and `minecraftVersion`;
2. run the data generator;
3. copy `<target>.txt` into the committed generated-item directory;
4. report the contents of `<target>-new-items.txt` and `<target>-uncategorized-items.txt`;
5. run the build and report compilation/test failures.

The following parts require review rather than blind automation:

- deciding the correct category for each new item;
- deciding whether a new default category should be introduced;
- identifying semantic item renames and migration mappings;
- adapting Box code to Paper/Bukkit API changes.

An automated agent should stop and request review when any of those decisions are required instead of inventing mappings or categories.
