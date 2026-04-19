# ForgeTools-SMP `v1.0.0`

Custom SMP tool plugin for Paper 1.21+ — 15 unique tools for terrain cleanup and assisted building, fully server-side.

---

## Requirements

| Dependency | Type | Version |
|---|---|---|
| Paper / Purpur | Required | 1.21+ |
| Java | Required | 21+ |
| WorldGuard | Soft | 7.0.12+ |
| GriefPrevention | Soft | 16.18+ |
| Towny | Soft | 0.100+ |
| CoreProtect | Soft | 22.4+ |
| Vault | Soft | 1.7+ |
| ItemsAdder / Oraxen | Optional | any |

All soft-dependencies are auto-detected at startup. The plugin works without any of them.

---

## Build

```bash
git clone https://github.com/forgetools/ForgeTools
cd ForgeTools
mvn clean package -DskipTests
# Output: target/ForgeTools-1.0.0.jar
```

Copy the JAR to your server's `plugins/` folder.

---

## Installation

1. Drop `ForgeTools-1.0.0.jar` into `plugins/`.
2. Start the server — `config.yml` and `tools.yml` are auto-generated in `plugins/ForgeTools/`.
3. Assign permissions via LuckPerms (see below).
4. Give tools with `/forgetools give <player> <tool_id>`.

---

## Commands

| Command | Permission | Description |
|---|---|---|
| `/forgetools give <player> <tool_id>` | `forgetools.admin.give` | Give a tool to a player |
| `/forgetools list` | `forgetools.command` | List all loaded tools |
| `/forgetools reload` | `forgetools.admin.reload` | Hot-reload config and tools.yml |
| `/forgetools setfilter <material>` | `forgetools.use.claim_polisher` | Set ClaimPolisher block filter |

Alias: `/ft`

---

## Permissions

```
forgetools.use.*                   — All tools (wildcard)
forgetools.use.chunk_hammer
forgetools.use.debris_vacuum
forgetools.use.terrain_eraser
forgetools.use.bridge_forge
forgetools.use.scaffold_launcher
forgetools.use.fill_pulse
forgetools.use.wall_raiser
forgetools.use.echo_picker
forgetools.use.gravity_anchor
forgetools.use.light_weaver
forgetools.use.resource_recycler
forgetools.use.auto_level_shovel
forgetools.use.claim_polisher
forgetools.use.phantom_placer
forgetools.use.biome_stitcher      — Op only by default
forgetools.admin.give
forgetools.admin.reload
```

---

## Tools Reference

### Category 1 — Terrain Cleanup

#### `chunk_hammer` — Chunk Hammer
- **Base:** Diamond Pickaxe
- **Energy:** 8/use | **Cooldown:** 8 ticks | **Max uses:** 500
- **Mode 0 — Line Break:** Breaks up to 3 blocks of the same type in a straight line from the target.
- **Mode 1 — Flat Clean 3×3:** Removes the topmost surface block in a 3×3 grid.
- **Switch mode:** Sneak + Right-click.

#### `debris_vacuum` — Debris Vacuum
- **Base:** Iron Hoe
- **Energy:** 5/use | **Cooldown:** 5 ticks | **Max uses:** 800
- Shoots a ray 6 blocks forward and vacuums debris (gravel, sand, leaves, vines, scaffolding) directly into inventory. Blocked areas show smoke particles.

#### `terrain_eraser` — Terrain Eraser
- **Base:** Netherite Shovel
- **Energy:** 12/use | **Cooldown:** 12 ticks | **Max uses:** 300
- Removes the topmost surface block within a configurable radius (default 4) for dirt, grass, sand, gravel, podzol, clay and related blocks.

---

### Category 2 — Assisted Building

#### `bridge_forge` — Bridge Forge
- **Base:** Golden Axe
- **Energy:** 3/block | **Max uses:** 1000
- **Mode 0 — Bridge:** While sneaking and walking, auto-places blocks from hotbar under your feet. Resets counter with Right-click.
- **Mode 1 — Rail Mode:** Same as Bridge, but also places oak fence railings on both sides.

#### `scaffold_launcher` — Scaffold Launcher
- **Base:** Bamboo
- **Energy:** 6/launch | **Cooldown:** 10 ticks | **Max uses:** 400
- **Mode 0 — Vertical:** Launches scaffolding straight up (max 8 blocks).
- **Mode 1 — Horizontal:** Launches scaffolding in the direction you face.
- Scaffolding auto-removes after 30 seconds (600 ticks, configurable).

#### `fill_pulse` — Fill Pulse
- **Base:** Amethyst Shard
- **Energy:** 25/pulse | **Cooldown:** 30 ticks | **Max uses:** 200
- Right-click a block with a placeable block in your **off-hand**. Fills all air blocks in a sphere of radius 2 around the clicked block.

#### `wall_raiser` — Wall Raiser
- **Base:** Brick
- **Energy:** 4/block | **Cooldown:** 8 ticks | **Max uses:** 600
- **Mode 0 — Straight:** Right-click a block face → raises a wall 5 blocks high, 5 blocks wide using hotbar material.
- **Mode 1 — 90° Curve:** Extrudes the wall outward from the clicked face instead of laterally.

---

### Category 3 — Unique Mechanics

#### `echo_picker` — Echo Picker
- **Base:** Diamond Pickaxe
- **Energy:** 10/use | **Cooldown:** 10 ticks | **Max uses:** 400
- **Mode 0 — Normal:** Breaks normally, remembers the last broken block type.
- **Mode 1 — Echo:** Flood-fills and breaks all connected blocks of the remembered type within radius 3 (max 25 blocks, BFS).

#### `gravity_anchor` — Gravity Anchor
- **Base:** Anvil
- **Energy:** 15/use | **Cooldown:** 20 ticks | **Max uses:** 250
- Makes all floating solid blocks within radius 4 fall as `FallingBlock` entities. Excludes bedrock, obsidian, command blocks.

#### `light_weaver` — Light Weaver
- **Base:** Glowstone Dust
- **Energy:** 12/use | **Cooldown:** 15 ticks | **Max uses:** 400
- **Mode 0 — Place Lights:** Places sea lanterns (configurable) in a cross pattern of radius 3. Consumes blocks from hotbar.
- **Mode 1 — Dark Mode:** Removes all light sources (torches, lanterns, glowstone, etc.) in the same cross pattern.

#### `resource_recycler` — Resource Recycler
- **Base:** Furnace Minecart
- **Energy:** 7/break | **Cooldown:** 6 ticks | **Max uses:** 600
- On block break, auto-smelts drops with a 50% chance (configurable). Smelted items go straight to inventory. Supports 22 recipes including ores → ingots, stone → smooth stone, sand → glass.

#### `auto_level_shovel` — Auto Level Shovel
- **Base:** Netherite Shovel
- **Energy:** 18/use | **Cooldown:** 25 ticks | **Max uses:** 250
- Levels a 5×5 area (radius 2) to the player's eye-level Y. Breaks blocks that are too high (±1 delta) and fills air with hotbar material.

#### `claim_polisher` — Claim Polisher
- **Base:** Netherite Hoe
- **Energy:** 30/use | **Cooldown:** 60 ticks | **Max uses:** 150
- Removes all blocks of a specific type in the entire current chunk, processed in batches of 25 blocks/tick to prevent lag.
- Set target material with `/forgetools setfilter <material>` before using.

#### `phantom_placer` — Phantom Placer
- **Base:** Ender Pearl
- **Energy:** 9/use | **Cooldown:** 12 ticks | **Max uses:** 500
- Raytrace-places blocks from hotbar at up to 15 blocks distance. Only places on positions that have a solid block below them.

#### `biome_stitcher` — Biome Stitcher
- **Base:** Moss Block
- **Energy:** 40/use | **Cooldown:** 40 ticks | **Max uses:** 100
- Changes the biome of a 16×16×16 section centered on the player. Cycle through biomes with Sneak + Right-click (plains → forest → desert → taiga → swamp → jungle → savanna → snowy plains → mushroom fields → cherry grove).
- Calls `world.refreshChunk()` after application for immediate visual update.
- **Default permission: Op only.**

---

## Energy System

Each tool has a **Forge Energy** bar (0–100). Energy:
- **Regenerates passively** at +1 per 4 ticks while the tool is anywhere in the player's inventory (configurable).
- **Charges manually** by holding a charging item in the off-hand and right-clicking while holding the tool in the main hand:

| Item | Energy Gain |
|---|---|
| Glowstone Dust | +15 |
| Iron Ingot | +20 |
| Blaze Rod | +30 |
| Gold Ingot | +35 |
| Diamond | +50 |
| Netherite Ingot | +75 |

Energy and mode are stored in `PersistentDataContainer` — they survive restarts and inventory transfers.

---

## Durability (Max Uses)

Every tool has a finite use count stored in PDC. When it hits zero the tool **breaks**: plays `ENTITY_ITEM_BREAK` sound, sends a red message, and is removed from the player's inventory. No vanilla durability is used — tools are `unbreakable: true` in metadata.

---

## Protection Compatibility

Before every block break or placement, ForgeTools checks in order:

1. **WorldGuard** — `Flags.BUILD` query via `RegionQuery`
2. **GriefPrevention** — `GriefPrevention.allowBuild()`
3. **Towny** — Town block resident check via `TownyAPI`

Op players bypass all checks. If a protection plugin is absent, that check is skipped silently.

---

## Anti-Lag Guarantees

- Maximum **25 blocks per tick** for all tools (configurable: `settings.max_blocks_per_tick`).
- `ClaimPolisher` uses a `BukkitRunnable` with 1-tick delay per batch — never processes an entire chunk in one tick.
- All block changes run on the **main thread** (Bukkit scheduler). No async block mutations.
- `BlockBreakEvent` is cancelled and drops are applied manually to prevent double-drop and recursion in `EchoPicker` and `ResourceRecycler`.
- `PlayerMoveEvent` in `BridgeForge` is guarded by `hasChangedBlock()` to avoid firing on head rotation.

---

## Configuration

### `config.yml`
```yaml
settings:
  energy_regen_interval_ticks: 4   # How often energy regens
  energy_regen_amount: 1           # Amount per regen tick
  max_blocks_per_tick: 25          # Hard cap per tool activation
  log_to_coreprotect: true
  check_worldguard: true
  check_griefprevention: true
  check_towny: true
  particles_enabled: true
  sounds_enabled: true
  lore_update_on_regen: true       # Set false to reduce lore writes
```

### `tools.yml`

Each tool section is independent. Example for `chunk_hammer`:
```yaml
chunk_hammer:
  display_name: "&6Chunk Hammer"
  base: DIAMOND_PICKAXE
  energy_max: 100
  energy_cost: 8
  cooldown_ticks: 8
  max_uses: 500
  radius: 3
  allowed_blocks:
    - STONE
    - DIRT
    - GRASS_BLOCK
```

Hot-reload with `/forgetools reload` — no restart needed.

---

## Architecture Overview

```
ForgeToolsPlugin          bootstrap, wiring
├── Keys                  static NamespacedKey cache (TOOL_ID, ENERGY, MODE, USES_REMAINING)
├── ToolRegistry          factory map, createToolItem(), getToolFromItem()
├── EnergyManager         BukkitRunnable — passive regen every N ticks
├── ProtectionManager     WorldGuard + GriefPrevention + Towny with graceful fallback
├── CoreProtectLogger     logRemoval() / logPlacement()
├── Listeners
│   ├── ToolInteractListener   RIGHT_CLICK → sneak=mode switch | else onRightClick()
│   ├── ToolBreakListener      BlockBreakEvent → tools with handlesBlockBreak()=true
│   ├── ToolMoveListener       PlayerMoveEvent → BridgeForge (hasChangedBlock guard)
│   └── EnergyChargeListener   OFF_HAND item → charges main-hand tool energy
├── CustomTool (abstract)
│   ├── canActivate()     cooldown + energy + permission in one call
│   ├── postAction()      single getItemMeta/setItemMeta — batches energy+uses+lore
│   ├── regenEnergy()     1 read + 1 write
│   └── breakTool()       PDC-based item lookup, sound, removal
├── Tools (15 implementations)
└── Util
    ├── BlockUtil         getTargetBlock, getSphereBlocks, floodFillSameType (BFS),
    │                     getHighestSolidBlock, getCrossPattern, consumeMaterial
    └── SmeltMap          22 ore→ingot / block→processed recipes
```

---

## Adding a New Tool

1. Create `MyTool extends CustomTool` in `com.forgetools.tools`.
2. Call `super(plugin, "my_tool_id", cfg)` — reads all base values from `tools.yml`.
3. Override `onRightClick()`, and optionally `onBlockBreak()` + `handlesBlockBreak()=true`, or `onMove()` + `handlesMove()=true`.
4. Call `canActivate(player, item)` at the top, `postAction(player, item)` at the bottom.
5. Add `"my_tool"` → `MyTool::new` to `ToolRegistry.FACTORIES`.
6. Add a section to `tools.yml`.
7. Add `forgetools.use.my_tool` permission to `plugin.yml`.

That's the entire integration surface — no other files need changing.
