# How to update the Minecraft version

This document describes the repeatable workflow for updating Box to a new Minecraft/Paper version.

The procedure is based on previous Minecraft update work, especially:

- [#490 - Minecraft 1.21.7](https://github.com/okocraft/Box/pull/490)
- [#525 - Minecraft 1.21.9 / 1.21.10](https://github.com/okocraft/Box/pull/525)
- [#564 - Minecraft 26.1](https://github.com/okocraft/Box/pull/564)
- [Minecraft 26.2 update commit](https://github.com/okocraft/Box/commit/b6b433951d78557caa558439bbffa3291b578a8a)
- [#614 - Minecraft 26.3](https://github.com/okocraft/Box/pull/614)

Older PRs contain version-specific module changes that are no longer required after the versioning refactor in June 2026. Follow the current repository structure described below.

## Inputs

Before starting, determine:

- the previous Minecraft version currently supported by Box;
- the target Minecraft version;
- a Paper API build compatible with the target version;
- the target Minecraft data version.

The data version is used to guard newly introduced items in the bundled category definitions and, when necessary, to define item rename migration boundaries.

Prefer processing released Minecraft versions sequentially when several versions were skipped. This makes item additions, removals, and renames easier to review and preserves migration mappings at the correct data-version boundary.

## 1. Update the Paper dependency

Check `gradle/libs.versions.toml`.

Renovate normally updates `io.papermc.paper:paper-api`, so this may already be done. If not, update `paper` to a build for the target Minecraft version.

Do not change unrelated dependencies as part of the Minecraft update. Update `paper-javadoc` only when a matching version is available and the project needs it; it does not have to move in the same commit as `paper`.

Then run:

```shell
./gradlew build
```

Fix compilation errors caused by Paper/Bukkit API changes before continuing. Previous updates required source changes outside the version/data files, so compiler errors must be treated as part of the update rather than bypassed.

## 2. Configure and run the data generator

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

### Ensure `runServer` stops automatically

A normal Paper server keeps running after the plugin has generated its data. The data generator therefore supports the system property:

```text
net.okocraft.box.datagenerator.auto-stop
```

When this property is enabled, `data-generator/src/main/java/net/okocraft/box/datagenerator/Main.java` must schedule shutdown after generation:

```java
private static final boolean AUTO_STOP =
    Boolean.getBoolean("net.okocraft.box.datagenerator.auto-stop");

@Override
public void onEnable() {
    this.generateData();
    if (AUTO_STOP) {
        Bukkit.getGlobalRegionScheduler().run(this, task -> Bukkit.shutdown());
    }
}
```

Use `Bukkit.getGlobalRegionScheduler()` so shutdown is scheduled for the next scheduler tick instead of stopping the server directly inside plugin enable processing.

The `runServer` task in `data-generator/build.gradle.kts` must enable the property:

```kotlin
runServer {
    minecraftVersion(minecraftVersion)
    systemProperty("com.mojang.eula.agree", "true")
    systemProperty("paper.disablePluginRemapping", "true")
    systemProperty("net.okocraft.box.datagenerator.auto-stop", "true")
    // ...
}
```

This infrastructure was introduced during the Minecraft 26.3 update. Once it exists, future version updates normally only need to keep it enabled rather than changing the implementation.

Run the generator with:

```shell
./gradlew :box-data-generator:runServer
```

The command should return by itself after data generation. A server that remains running indicates that auto-stop is not working and should be treated as a failed generation run.

The relevant outputs are:

```text
<target>.txt
<target>-new-items.txt
<target>-uncategorized-items.txt
```

- `<target>.txt` is the complete generated default-item list.
- `<target>-new-items.txt` is the difference from the previous version after known renames are applied.
- `<target>-uncategorized-items.txt` contains default items that are not covered by the bundled category definitions.

### Alternative: generate with a temporary GitHub Actions workflow

If the generator cannot be run locally, add a temporary workflow under `.github/workflows/` on the update branch and upload the generated directory as an artifact.

Use the repository's current Java and action versions. The following is a template; replace the placeholders for each update:

```yaml
name: Minecraft <target> data generation

on:
  push:
    branches: [<update-branch>]

jobs:
  generate:
    runs-on: ubuntu-latest
    timeout-minutes: 15

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '25'

      - uses: gradle/actions/setup-gradle@v4

      - run: chmod +x ./gradlew

      - name: Generate data
        run: ./gradlew :box-data-generator:runServer

      - name: Check generated files
        run: |
          test -s data-generator/build/resources/generated-data/<target>.txt
          test -f data-generator/build/resources/generated-data/<target>-new-items.txt
          test -f data-generator/build/resources/generated-data/<target>-uncategorized-items.txt

      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: minecraft-<target>-data
          path: data-generator/build/resources/generated-data/
          if-no-files-found: error
```

Do not make a shell timeout count as a successful generation. The workflow-level timeout is only a safety limit; `runServer` must terminate normally through auto-stop.

After the workflow succeeds:

1. download the `minecraft-<target>-data` artifact;
2. inspect the generated full list, new-item list, and uncategorized list;
3. make the required repository changes;
4. rerun the workflow to validate the final category coverage if necessary;
5. remove the temporary workflow from the branch before the update PR is considered complete.

The Minecraft 26.3 update used this approach because the generator could not be run locally. The temporary workflow was removed after the generated data and category coverage had been verified.

## 3. Commit the generated item list

Copy:

```text
data-generator/build/resources/generated-data/<target>.txt
```

to:

```text
data-generator/src/main/resources/generated/items/<target>.txt
```

When using the GitHub Actions fallback, copy the same file from the downloaded artifact.

The `-new-items.txt` and `-uncategorized-items.txt` files are review aids and are not normally committed.

Compare the generated complete list against the previous version as an additional sanity check. Unexpected removals may indicate an identifier rename or another compatibility issue that needs investigation.

## 4. Categorize new items

Review both diagnostic files, especially `<target>-new-items.txt` and `<target>-uncategorized-items.txt`.

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

### How to decide categories

When the task includes categorizing new items, do not stop merely because classification requires judgment. Proceed with classification when there is sufficient evidence from:

- existing Box categories and how similar items are already grouped;
- item families and naming patterns in the generated data;
- official Minecraft release notes, changelogs, or other official descriptions of the new content.

After classifying, rerun the generator and use the uncategorized output to verify coverage.

Ask for clarification only when the existing category structure and official change information still do not provide a defensible classification. Do not invent a new semantic grouping when the available evidence is genuinely ambiguous.

### Place new items consistently within each category

Do not append all new items to the end of a category as one block. Preserve the category's existing organization and place each new item near the most closely related existing items.

Use the local ordering pattern already present in the category as the primary guide. For example:

- when a category is grouped by material or family, insert the new family alongside the corresponding existing families;
- when a category is grouped by shape or variant, keep new slabs, stairs, carpets, beds, and similar variants in the corresponding shape block;
- when an item extends a specific base item or concept, place it immediately after or near that base item;
- for plants and other natural items, place them near the most closely related existing plants instead of at the category boundary.

Minecraft 26.3 is an example of this placement rule:

- Poplar items were inserted into the existing wood-type ordering in `woods-2`;
- Concrete and Wool slabs and stairs were grouped with the existing shape-based sections instead of being appended after all older entries;
- Explorer Map items were placed immediately after `MAP` in `tools`;
- `SHELF_MUSHROOM` and `RED_SHRUB` were placed near related plant entries in `farms` and `flowers`.

Placement changes must not alter the classification itself. After reorganizing the entries, compare the state before and after reordering and verify that:

- the number of newly added items is unchanged;
- every new item remains in the same category;
- every new item retains the same Minecraft data-version guard.

A useful invariant is the set of `(category, data-version, item)` tuples for the target version: reordering may change line positions, but it must not change that set.

For example, Minecraft 26.3 added 121 generated item identifiers and removed none compared with 26.2. The update used data version 5023 and classified the new items into existing categories, including Poplar items under `woods-2`, concrete variants under `concretes`, wool/cushion/bed items under `wools`, and explorer-map items under `tools`. The placement was then adjusted according to the rules above without changing the 121-item count, category assignments, or data version. These numbers and data version are historical facts for the 26.3 update only; always derive the corresponding values again for future versions.

After editing the categories, rerun:

```shell
./gradlew :box-data-generator:runServer
```

The goal is for `<target>-uncategorized-items.txt` to exist and be empty.

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

Minecraft 26.3 had no removed generated identifiers compared with 26.2, so no rename migration was needed in that update. Do not assume this will be true for later versions.

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

If local generation is unavailable, perform the equivalent generator checks in the temporary GitHub Actions workflow and download its artifact.

Verify that:

- `<target>-uncategorized-items.txt` exists and is empty;
- `<target>-new-items.txt` contains only genuinely new items after known renames are applied;
- after any category-ordering cleanup, the count and set of `(category, data-version, item)` tuples for newly added items are unchanged;
- the generated complete `<target>.txt` and the committed `data-generator/src/main/resources/generated/items/<target>.txt` are identical;
- the previous and target complete lists have been compared for unexpected removals;
- `runServer` exits after generation and the Paper server does not remain running;
- every rename has a migration resource and is registered in `RenamedItems.VERSIONS`;
- the project compiles and tests pass;
- any Paper/Bukkit API breakage has been handled intentionally;
- any temporary GitHub Actions workflow used for generation has been removed from the final branch.

For a strong equality check between the generated and committed full lists, compare the files byte-for-byte or compare their Git blob hashes. Minecraft 26.3 was verified by matching the generated artifact's complete list with the committed `26.3.txt`.

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

The auto-stop support added during the Minecraft 26.3 update also changed:

```text
data-generator/src/main/java/net/okocraft/box/datagenerator/Main.java
```

That is a one-time data-generator infrastructure change, not a file that should normally change for every Minecraft version.

Other source files should only change when the new Paper/Bukkit API requires compatibility fixes.

## Automation boundary

The following parts are suitable for automation:

1. update `previousMinecraftVersion` and `minecraftVersion`;
2. run the data generator locally or through a temporary GitHub Actions workflow;
3. copy `<target>.txt` into the committed generated-item directory;
4. compare the previous and target complete item lists;
5. report the contents of `<target>-new-items.txt` and `<target>-uncategorized-items.txt`;
6. run the build and report compilation/test failures;
7. verify that the generator terminates and that the committed complete list matches the generated output.

The following parts require evidence-based review rather than blind automation:

- assigning new items to categories and placing them consistently with the category's existing ordering;
- deciding whether a new default category should be introduced;
- identifying semantic item renames and migration mappings;
- adapting Box code to Paper/Bukkit API changes.

If the requested task includes classification, an automated agent should use the existing category structure and official Minecraft change information to classify and validate the new items instead of stopping at the first judgment call. It should request clarification only when those sources do not provide enough evidence for a defensible decision.
