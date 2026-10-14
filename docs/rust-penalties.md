# Rust Penalties

Rust penalties apply to this mod's shafts, small cogwheels, large cogwheels, and all andesite/brass encased variants. Wax preserves the current oxidation stage and its penalty; it does not repair existing oxidation. Fresh copper has no additional stress cost.

## Configuration

NeoForge registers these settings as a server configuration, shared with connected clients. The generated file is `config/create_rusty_copper-server.toml`, with optional per-world overrides in `<world>/serverconfig/create_rusty_copper-server.toml`. The mod list's configuration screen exposes the same settings, with English and Simplified Chinese translations.

```toml
[rustPenalty]
enabled = true
exposedStressImpact = 8.0
weatheredStressImpact = 32.0
oxidizedStressImpact = 1.0E9
```

| Setting | Default | Effect |
| --- | --- | --- |
| `enabled` | `true` | Master switch for all rust penalties |
| `exposedStressImpact` | `8.0` | Additional SU per RPM for each exposed component |
| `weatheredStressImpact` | `32.0` | Additional SU per RPM for each weathered component |
| `oxidizedStressImpact` | `1,000,000,000.0` | Additional SU per RPM for each oxidized component |

Each stage accepts values from **0** to **1,000,000,000,000**. Zero disables that stage's penalty. Values are absolute additional costs, since copper transmission components otherwise consume zero stress. For example, one exposed component at 16 RPM consumes **128 SU**, while one weathered component at 32 RPM consumes **1,024 SU**. Different gear ratios use each component's own absolute RPM.

Changes to loaded settings update loaded copper components on their next server tick. Oxidation, scraping, waxing, and wax removal immediately refresh the affected network member's stress cost. Loaded block entities also refresh restored network totals, so configuration changes made while a world is closed take effect when its chunks load.

## Network shutdown and recovery

Fully oxidized copper uses a very large **finite** stress cost rather than an absolute mechanical lock. At 16 RPM, the default consumes **16 billion SU per oxidized component**. This overloads ordinary Create networks and stops the motor, transmission, and connected machines through Create's existing overstress mechanism. Disconnected networks keep operating. Sufficiently large capacity can overcome this cost; it is deliberately configurable.

The network retains theoretical RPM while overloaded. This keeps its stress demand from dropping to zero and repeatedly restarting. Scraping an oxidized component down to a lower stage, removing or replacing it, reducing the configured cost, or disabling rust penalties lets the network resume once total stress fits its capacity. If other oxidized components remain, their penalties still apply.

Create's `kinetics.disableStress = true` bypasses stress-based shutdown, including this feature. Rust penalties do not change Create's ordinary shafts, cogwheels, or other components.

## Implementation and verification

`CopperStress` registers dynamic suppliers in Create 6.0.10's `BlockStressValues.IMPACTS` registry, keeping stress calculations and Create's goggle readouts consistent. Appearance transitions refresh the stored member impact without replacing the block entity, brackets, or network membership. A scoped `KineticBlockEntity` mixin checks copper members for configuration changes and recalculates restored networks that contain copper components. Newly loaded members also synchronize their saved overload state, even when network totals do not change.

Run:

```sh
./gradlew build
./gradlew runGameTestServer
```

`CopperStressGameTests` checks all 72 block variants, stress scaling across gear ratios, actual random-tick oxidation and axe/honeycomb interactions, entire-network shutdown, separate-network isolation, persistent overload, recovery by scraping and replacement, live configuration changes, and saved block-entity restoration after configuration changes in different loading orders. Configuration-mutating tests run in separate batches and restore previous settings. Existing transmission tests run with penalties disabled to preserve their original purpose; the dedicated stress tests cover enabled behavior.

These are server gameplay checks. Client animation and the configuration screen's visual presentation require a development-client check.
