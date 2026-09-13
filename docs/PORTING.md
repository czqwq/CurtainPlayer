# Curtain 1.20.1 -> 1.7.10 port notes

Source: `tmp/Curtain-1.20.1` (Curtain 1.3.2, Forge 1.20.1, package `dev.dubhe.curtain`).
Target: this repository (Minecraft 1.7.10 / Forge 10.13.4.1614, GTNH RetroFuturaGradle + UniMixins,
package `com.Lilith.Curtain`).

The architecture, the rule engine, the command surface, the fake player and every logger were ported
one to one. Only the parts that reference content which does not exist in 1.7.10 were adapted or left
out, see "Rules" below.

## API mapping used by the port

| 1.20.1 | 1.7.10 |
| --- | --- |
| `Level` / `ServerLevel` | `World` / `WorldServer` |
| `ServerPlayer` | `EntityPlayerMP` |
| `ServerPlayerGameMode` | `ItemInWorldManager` |
| `BlockPos` / `Direction` | `int x, y, z` / `ForgeDirection`, `EnumFacing`, `Facing` |
| `BlockState` | `Block` + `int metadata` |
| `Component` / `MutableComponent` | `IChatComponent` / `ChatComponentText` + `ChatStyle` |
| `ClickEvent` / `HoverEvent` | `net.minecraft.event.ClickEvent` / `HoverEvent` |
| Brigadier `CommandDispatcher` | `ICommand` / `CommandBase` + `addTabCompletionOptions` |
| `Container` (menus) | `IInventory` + `Container` + `IGuiHandler` + `GuiContainer` |
| `MobCategory` | `EnumCreatureType` |
| `NaturalSpawner` | `SpawnerAnimals` |
| `Explosion.BlockInteraction` | `Explosion.isFlaming` / `isSmoking` |
| `ItemStack.getTag()` | `ItemStack.getTagCompound()` |
| Forge 1.20 event bus | `MinecraftForge.EVENT_BUS` (Forge events) + `FMLCommonHandler.instance().bus()` (Tick/Player events) |

## Layout

```
com/Lilith/Curtain/
  Curtain.java                 mod entry point (1.20: @Mod with FMLJavaModLoadingContext)
  ICurtain.java                sub mod API
  CurtainRules.java            every rule (the 1.20 file, minus the rules listed below)
  Tags.java                    generated version holder
  api/Function.java            same
  api/PlanExecution.java       same (used by fake player auto fish)
  api/menu/CustomMenu.java     Container -> IInventory
  api/menu/control/*           Button, ButtonList, CheckList, RadioList, AutoResetButton
  api/rules/*                  Rule, RuleManager, CurtainRule, Categories, Validators, IValidator, RuleException
  commands/RuleCommand.java    /curtain  (was Brigadier)
  commands/PlayerCommand.java  /player   (was Brigadier, all sub commands kept)
  commands/LogCommand.java     /log
  events/*                     lifecycle, server, player and interaction handlers
  features/logging/*           LoggerManager, TPS/Mobcaps/Memory logger, explosion and TNT loggers
  features/player/*            fake player, action pack, inventory menu, resident persistence
  mixins/*                     all mixins (see mixins.curtain.json)
  utils/*                      Messenger, MenuHelper, TranslationHelper, BlockRotator, SpawnReporter,
                               OptimizedExplosion, Tracer, CurtainGuiHandler, ...
```

## Rules

Everything from `CurtainRules` was ported except the rules that depend on 1.8+ content:

| rule | why it is missing |
| --- | --- |
| `stackableShulkerBoxes` | shulker boxes do not exist in 1.7.10 |
| `emptyShulkerBoxStackAlways` | same |
| `scaffoldingDistance` | scaffolding does not exist in 1.7.10 |
| `turtleEggTrampledDisabled` | turtles and turtle eggs do not exist in 1.7.10 |
| `betterWoodStrip` | stripping logs (and therefore the rule) was added in 1.13 |
| `creativeNoClip` client rendering | there is no spectator mode in 1.7.10; no-clip is implemented through `Entity.noClip` |
| `antiCheatDisabled` elytra part | elytra do not exist in 1.7.10, the floating-tick part is ported |

Adaptations of ported rules:

* `viewDistance` uses `ServerConfigurationManager.func_152611_a(int)` (the 1.7.10 equivalent of
  `PlayerList#setViewDistance`).
* `fillUpdates` is declared but has no effect: 1.7.10 has no `/fill`, `/clone`, `/setblock` or
  structure blocks. `interactionUpdates` is honoured by the block rotator.
* `desertShrubs` considers a biome "hot and dry" when `temperature >= 1.0` or the temperature category
  is `WARM`, and scans a 9x9x6 box for water.
* `quickLeafDecay` schedules an update tick for the leaves around a broken log (1.7.10 leaves only
  decay on random ticks).
* `betterFenceGatePlacement` keeps the facing of the gate that is already in the target position.
* `betterSignInteraction` forwards the interaction of a wall sign to the block behind it (1.7.10
  cannot edit existing signs).
* `missingTools` is applied in `ItemPickaxe#func_150893_a`.
* `swingHands` / `drop offhand` report that 1.7.10 has no offhand slot.
* HUD loggers (tps, mobcaps, memory) are printed to the chat when their content changed, because
  1.7.10 has no tab list header/footer packet.

## Fake players

1.7.10 only ticks the *body* of a player entity when the client sends movement packets
(`NetHandlerPlayServer#processPlayer` calls `EntityPlayerMP#onUpdateEntity`), so
`EntityPlayerMPFake#onUpdate` drives `onUpdateEntity()` explicitly.

The fake connection is a `NetworkManager` subclass (`FakeNetworkManager`) that owns an
`EmbeddedChannel` and drops every outbound packet; the embedded channel is injected through the
`NetworkManagerMixin` accessor (`IClientConnection`), exactly like the 1.20
`FakeClientConnection` does. The player is registered through
`ServerConfigurationManager#initializeConnectionToPlayer(NetworkManager, EntityPlayerMP, NetHandlerPlayServer)`.

Because 1.7.10 has no offhand slot, the fake player inventory GUI has 54 slots:
0 = STOP ALL, 1-4 = armor, 5-13 = hotbar selection, 14-16 = attack/use buttons, 18-53 = the fake
player inventory. Clicking a button slot toggles the rule through `IButtonContainer`.

## Chat output in 1.7.10

Two differences to 1.20 that shaped the port:

* **One chat message per line.** 1.7.10 cannot render a line break inside a chat component, so
  `/curtain` and every logger return a `List<IChatComponent>` and each line is sent with its own
  `addChatMessage`. `MenuHelper.main()/category()`, `AbstractLogger#display` and
  `LoggerManager#updateHUD` follow that contract.
* **The client localizes, not the server.** Player facing text uses `ChatComponentTranslation`
  (`TranslationHelper.translate`), so the server only sends the key and the client renders it in the
  player's own language, nested components included (rule names and descriptions are arguments of the
  menu lines). The translations ship as classic 1.7.10 language files
  (`assets/curtain/lang/en_US.lang` and `zh_CN.lang`, generated from the upstream JSON files, which are
  still loaded for the server side table).
  `TranslationHelper.translateLiteral` is used where a plain string is needed on the server
  (item display names of the menu buttons, console output, RULES.MD generation); the `language` rule
  selects that table.

## Fake player spawn position

`ServerConfigurationManager#initializeConnectionToPlayer` positions a fresh player at the world spawn and
only afterwards calls `playerLoggedIn(player)`, which adds the entity to the world - and the entity tracker
broadcasts that (wrong) position immediately. `EntityPlayerMPFake#fixStartingPosition` is therefore applied
by `ServerConfigurationManagerMixin` at the head of `playerLoggedIn`, so the very first broadcast already
carries the coordinates given to `/player <name> spawn at ...`.

## Runtime namespaces (1.7.10 specific pitfall)

MCP marks every member with a side: **0 = client only, 1 = server only, 2 = both** (methods.csv/fields.csv).
The GTNH hotswap client (`runClient17/21/25`) loads the mod's dev classes (MCP names) into a production
runtime (SRG names) and remaps the call sites with the legacy dev remapper. Names that are missing from the
client mapping table then fail with `NoSuchMethodError` inside the client JVM - including the integrated
server of single player. For example `MinecraftServer.getServerPort()` (side=1) and
`WorldSettings.GameType.getByName()` (side=0) are such traps.

This port avoids them completely:

* `PlayerCommand#maxPlayerLength` uses `FMLCommonHandler.instance().getSide()` instead of the server port,
  so a single player world (even when opened to LAN) keeps the 40 character limit and only a dedicated
  server uses 16.
* game modes are parsed by `CommandHelper.parseGameType`, which resolves the enum constants directly
  (`SURVIVAL`, `CREATIVE`, `ADVENTURE` are not renamed by the obfuscator).

`tools/namespace-audit.ps1` re-runs the exhaustive check: it classifies every `net/minecraft` reference of
the compiled classes against `mcp-srg.srg` plus methods.csv/fields.csv, following the inheritance chain, and
fails loudly if a side=1 reference appears outside dedicated-server-only code.

## Mixins

`usesMixins = true`, `mixinsPackage = mixins` in `gradle.properties` and
`src/main/resources/mixins.curtain.json` (UniMixins, refmap `mixins.curtain.refmap.json`).
The GTNH convention adds UniMixins, the MixinConfigs manifest entry and the annotation processor.