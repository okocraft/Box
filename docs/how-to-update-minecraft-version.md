# How to update the Minecraft version

This document describes the workflow for updating Box to a new Minecraft/Paper version using the current repository structure.

## 1. Prepare the target version

Determine:

- the currently committed Minecraft version;
- the target Minecraft version;
- a Paper API build compatible with the target version;
- the Minecraft data version for the target version.

Check `gradle/libs.versions.toml`. Renovate may already have updated `paper`; otherwise update it to a compatible build.

Run:

```shell
./gradlew build
```

Resolve compilation errors caused by Paper/Bukkit API changes as part of the version update.

## 2. Configure and run the data generator

Edit `data-generator/build.gradle.kts`:

```kotlin
val previousMinecraftVersion = "<previous>"
val minecraftVersion = "<target>"
```

`previousMinecraftVersion` must match an existing file:

```text
data-generator/src/main/resources/generated/items/<previous>.txt
```

Run:

```shell
./gradlew :box-data-generator:runServer
```

The existing `runServer` configuration enables `net.okocraft.box.datagenerator.auto-stop`, so the server must stop automatically after generation. If the process remains running, treat the generation as failed and fix the auto-stop behavior before continuing.

Generated files are written to:

```text
data-generator/build/resources/generated-data/
```

The relevant outputs are:

```text
<target>.txt
<target>-new-items.txt
<target>-uncategorized-items.txt
```

- `<target>.txt`: complete default-item list for the target version.
- `<target>-new-items.txt`: items not present in the previous version after known rename mappings are applied.
- `<target>-uncategorized-items.txt`: items missing from the default categories. Each line includes the current Minecraft data version; use that generated value when adding version guards.

Do not reuse a data-version value from an older Minecraft update.

## 3. Update `MCDataVersion`

Every Minecraft version that Box explicitly supports must have a corresponding constant in:

```text
api/src/main/java/net/okocraft/box/api/util/MCDataVersion.java
```

After confirming the target data version, add the target version constant in the existing chronological order:

```java
public static final MCDataVersion MC_<version> = new MCDataVersion(<data-version>);
```

Also review the supported versions between the previously committed version and the target version. If Box already added support for an intermediate Minecraft version but its `MCDataVersion` constant is missing, add that constant as part of the update instead of leaving a gap.

Confirm each data-version value from the target Minecraft/Paper runtime or another authoritative source. Values from older updates are examples only and must not be reused for future versions.

For example, PR #615 filled two missing supported-version constants:

```java
MC_26_2 = 4903
MC_26_3 = 5023
```

Those values apply only to Minecraft 26.2 and 26.3.

Adding an `MCDataVersion` constant is independent from item rename handling. Do not add an entry to `RenamedItems.VERSIONS` or create a rename resource unless item identifiers actually changed.

### If the generator cannot run locally

Create a temporary workflow under `.github/workflows/` on the update branch. It should:

1. check out the branch;
2. use the repository's current Java/Gradle setup;
3. run `./gradlew :box-data-generator:runServer`;
4. verify that the three generated files exist;
5. upload `data-generator/build/resources/generated-data/` as an artifact.

Use a workflow-level timeout only as a failure safeguard. Do not make a timed-out `runServer` invocation count as success; the command must terminate normally through auto-stop.

Download the artifact, use it for the update and verification, then delete the temporary workflow before the PR is complete.

## 4. Commit the generated complete list

Copy:

```text
data-generator/build/resources/generated-data/<target>.txt
```

to:

```text
data-generator/src/main/resources/generated/items/<target>.txt
```

If generation ran in GitHub Actions, copy the file from the downloaded artifact instead.

Compare the previous and target complete lists. Investigate any removed identifiers because they may indicate item renames or other compatibility changes.

The `-new-items.txt` and `-uncategorized-items.txt` files are review artifacts and are not committed.

## 5. Categorize new items

Use `<target>-new-items.txt` and `<target>-uncategorized-items.txt` to update:

```text
features/category/src/main/resources/default_categories.yml
```

Add newly introduced items with the data version emitted by the generator:

```yaml
  - <data-version>:<ITEM_NAME>
```

Classify items using, in order:

1. existing Box categories and the placement of similar items;
2. item-family and naming patterns;
3. official Minecraft release notes or changelogs.

If the task includes classification, continue through classification and validation when these sources provide a defensible answer. Ask for clarification only when they do not.

Items that should not be available as normal Box items belong in the existing `unavailable` category rather than being left uncategorized.

If the release introduces a group that genuinely requires a new default category, update both:

```text
features/category/src/main/resources/default_categories.yml
features/category/src/main/java/net/okocraft/box/feature/category/internal/category/defaults/DefaultCategories.java
```

### Preserve category ordering

Do not append all new items to the end of a category. Follow the local ordering already used in that category and place each item near related existing entries.

Current ordering patterns include:

- wood families in `woods-2`: keep each new wood type aligned with the corresponding planks, slabs, stairs, logs, wood, leaves, saplings, signs, boats, and other wood variants;
- `concretes` and `wools`: group variants by shape/type, such as slabs together and stairs together;
- `tools`: place specialized map items immediately after `MAP`;
- plant-related categories such as `farms` and `flowers`: place new plants near the most closely related existing plants.

Reordering must not change classification. After placement cleanup, verify that the newly added items have exactly the same:

- item count;
- category assignments;
- data-version guards.

Equivalently, the set of `(category, data-version, item)` tuples for the target version must be unchanged by reordering.

Rerun the generator after category changes:

```shell
./gradlew :box-data-generator:runServer
```

`<target>-uncategorized-items.txt` must exist and be empty.

## 6. Handle renamed items

If the previous and target lists indicate that an identifier was renamed, add:

```text
item-provider/src/main/resources/<data-version>.txt
```

with mappings in this format:

```text
OLD_NAME:NEW_NAME
```

Then:

1. register the already-added version constant in `RenamedItems.VERSIONS` in
   `item-provider/src/main/java/net/okocraft/box/item/RenamedItems.java`;
2. update affected category entries using the existing rename syntax when necessary;
3. rerun the generator and confirm the renamed item is not incorrectly reported as new.

`RenamedItems.VERSIONS` and `item-provider/src/main/resources/<data-version>.txt` are rename-specific. Update them only when an item identifier actually changed; they are not required merely because a new Minecraft version is supported.

## 7. Verify the update

Run:

```shell
./gradlew :box-data-generator:runServer
./gradlew build
```

If local generation is unavailable, perform the generator checks in the temporary GitHub Actions workflow and run the normal project CI/build checks.

Verify all of the following:

- `runServer` terminates automatically after generation;
- `<target>-uncategorized-items.txt` exists and is empty;
- `<target>-new-items.txt` contains only genuinely new items after rename handling;
- the generated complete `<target>.txt` is byte-for-byte identical to the committed `data-generator/src/main/resources/generated/items/<target>.txt`;
- removed identifiers from the previous complete list have been explained or handled;
- the target Minecraft version has the correct `MCDataVersion` constant;
- supported intermediate Minecraft versions do not have missing `MCDataVersion` constants;
- category ordering cleanup did not change the new items' count, category assignments, or data-version guards;
- when renames exist, every rename migration is registered in `RenamedItems.VERSIONS`;
- the project builds and tests pass;
- any temporary GitHub Actions workflow has been removed.

## Files commonly changed

A normal version update usually changes:

```text
data-generator/build.gradle.kts
data-generator/src/main/resources/generated/items/<target>.txt
features/category/src/main/resources/default_categories.yml
api/src/main/java/net/okocraft/box/api/util/MCDataVersion.java
```

Depending on the update, it may also change:

```text
gradle/libs.versions.toml
features/category/src/main/java/net/okocraft/box/feature/category/internal/category/defaults/DefaultCategories.java
item-provider/src/main/java/net/okocraft/box/item/RenamedItems.java
item-provider/src/main/resources/<data-version>.txt
```

Other source files should change only when required by Paper/Bukkit API compatibility or another concrete behavior change.

## Automation guidance

An automated update can perform the version edits, data-version lookup, `MCDataVersion` constant updates, missing-intermediate-constant checks, generation, full-list comparison, generated-file copy, category coverage check, build, and final invariants.

When classification is part of the requested work, it should also classify and place new items using the existing category structure and official Minecraft information. It should stop for clarification only when those sources do not support a defensible classification or rename decision.
