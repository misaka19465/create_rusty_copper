# Copper Weathering

## Source references

The implementation follows the mapped Minecraft **1.21.1** sources resolved by the Gradle wrapper with **NeoForge 21.1.219**. The relevant upstream classes are:

| Source path | Behavior reused |
| --- | --- |
| `net/minecraft/world/level/block/ChangeOverTimeBlock.java` | Random attempt probability, neighborhood scan, and age-based oxidation probability |
| `net/minecraft/world/level/block/WeatheringCopper.java` | Four weathering ages, stage transitions, first-stage lookup, and the fresh-copper chance modifier |
| `net/minecraft/world/level/block/WeatheringCopperFullBlock.java` | Dispatch from a block's random tick into `changeOverTime` |
| `net/minecraft/world/item/AxeItem.java` | Scraping and wax removal, tool wear, shield use intent, sounds, particles, game events, and advancement hooks |
| `net/minecraft/world/item/HoneycombItem.java` | Waxing, honeycomb consumption, particles, game events, and advancement hooks |
| `net/minecraft/world/entity/LightningBolt.java` | Lightning cleaning through the `WeatheringCopper` interface and stage lookups |
| `net/neoforged/neoforge/common/extensions/IBlockExtension.java` | The `AXE_SCRAPE` and `AXE_WAX_OFF` tool abilities used by the patched vanilla axe |
| `net/neoforged/neoforge/common/DataMapHooks.java` | Oxidation and wax lookups, including their inverse mappings |

The mod calls these runtime implementations. Minecraft's source files are not bundled with the mod.

## Oxidation

Every unwaxed shaft, small cogwheel, large cogwheel, and andesite/brass encased variant implements `WeatheringCopper` through `WeatheringCopperKineticBlock`. Their age uses the same `WeatherState` enum as vanilla copper, so vanilla and mod copper influence each other during neighborhood scans.

The four unwaxed block classes forward random ticks to vanilla `changeOverTime`:

1. A random tick attempts oxidation when `random.nextFloat() < 0.05688889F`.
2. The algorithm scans other weathering blocks within Manhattan distance **4**. Any younger copper in range cancels the attempt.
3. Let `older` count older neighbors and `same` count equally aged neighbors. The second random check must be below `((older + 1) / (older + same + 1))² × modifier`.
4. The modifier is **0.75** for fresh copper and **1.0** for exposed or weathered copper. A successful attempt advances one stage.

Random tick eligibility depends on the registered age. Oxidized and waxed blocks do not receive random ticks. This age check is necessary because Minecraft initializes its block-state random-tick cache before data maps load; asking the data map for a custom block's next stage during initialization would disable its random ticks.

Waxed blocks use separate classes without `WeatheringCopper`. They are excluded from the neighborhood counts and vanilla lightning cleaning, as vanilla waxed copper is.

## Scraping and waxing

`src/main/resources/data/neoforge/data_maps/block/oxidizables.json` supplies **27** forward oxidation transitions across the nine component families. NeoForge derives the inverse transitions used by vanilla axe scraping. Each scrape removes one stage.

`src/main/resources/data/neoforge/data_maps/block/waxables.json` supplies **36** unwaxed-to-waxed mappings. Vanilla honeycomb use applies wax, and NeoForge derives the inverse mappings used by vanilla axe wax removal. The first axe use on waxed copper removes only the wax; later uses remove oxidation.

Both paths preserve shared block-state properties through vanilla `withPropertiesOf`, including axis, waterlogging where supported, and encased cogwheel shaft connections. The vanilla item handlers provide their original consumption, durability, advancement, sound, particle, shield, and game-event behavior.

## Create compatibility

Create 6.0.10 normally destroys a block entity when its block ID changes, and bare shafts/cogwheels also remove attached brackets. Copper stage and wax changes require retaining those objects.

`CopperKineticBlock.isAppearanceChange` accepts a replacement only when the old and new blocks occupy the same position in their variant's component list and have identical block-state property values. The four copper base classes retain the block entity for those replacements. Their placement hook updates its block state, refreshes the network's stress cost, suppresses unnecessary kinetic propagation, and marks it for synchronization. This preserves brackets and network membership during vanilla block replacement. Rotation continues while the network has enough stress capacity; fully oxidized components can overload it according to the [rust penalty configuration](rust-penalties.md).

Other component replacements, casing changes, axis changes, and block destruction continue through Create's normal lifecycle.

## Verification

Run the compile/package checks and the dedicated server tests from the project root:

```sh
./gradlew build
./gradlew runGameTestServer
```

`CopperWeatheringGameTests` exercises all nine component families across all four ages and their waxed counterparts. It checks actual vanilla item use, tool wear, honeycomb consumption, property retention, random-tick eligibility, deterministic probability boundaries, vanilla/mod neighbor interactions, wax protection, shield intent, bracket retention, and continuous powered transmission. Uncontrolled random ticks are disabled for that test batch and restored afterward; oxidation tests invoke the real random-tick entry point with controlled random samples.

The existing kinetic tests remain part of the same server run. Server GameTests verify gameplay logic; client rendering and the visible particle/sound presentation still require a development-client check.
