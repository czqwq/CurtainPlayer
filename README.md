# Curtain (1.7.10 / GTNH port)

A port of [Curtain](https://github.com/Gu-ZT/Curtain) (a Forge mod like fabric-carpet, for Minecraft
1.20.1) to **Minecraft 1.7.10 / Forge 10.13.4.1614** (GTNH toolchain, RetroFuturaGradle + UniMixins).

* See live server data (TPS, mobcaps, memory, explosions, TNT) with `/log`
* Control and spawn players with `/player`
* Open the rule menu with `/curtain`
* Toggle rules with `/curtain setValue <rule> <value>`

## Commands

| command | description |
| --- | --- |
| `/curtain` | chat menu with every rule and its current value |
| `/curtain category <name>` | rules of one category |
| `/curtain setValue <rule> <value>` | change a rule for this session |
| `/curtain setDefault <rule> <value>` | change a rule and persist it in `world/curtain.json` |
| `/player <name> <action> ...` | spawn/control fake players |
| `/log <logger>` | subscribe/unsubscribe a logger |

## Rules

* [docs/RULES.md](docs/RULES.md) - the upstream rule list
* [docs/PORTING.md](docs/PORTING.md) - what changed for 1.7.10, and why a few rules do not exist here

## Building

```
./gradlew build
```

The resulting jar needs **UniMixins** at runtime (the GTNH build adds it as a dependency automatically).
