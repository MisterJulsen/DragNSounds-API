---
hide:
  - navigation
---
# Getting started
To start using the library in your mod, follow the steps below.
You can find all available major versions of this mod on CurseForge or Modrinth.

#### **Latest versions**

| Minecraft | Version |
| -------- | -------- |
| 1.18.2 | 0.1.0 |

## Architectury Loom setup
1. Create a new [Architectury project](https://docs.architectury.dev/plugin/get_started).
2. Add the following content into the `build.gradle` file of your root project.
```groovy
subprojects {
    repositories {
        maven { // DragonLib, DragNSounds API
            name = "MrJulsen's Mod Resources"
            url = "https://raw.githubusercontent.com/MisterJulsen/modsrepo/main/maven"
        }
        maven { // Forge Config Api (required for fabric version of DragonLib)
            name = "Fuzs Mod Resources"
            url = "https://raw.githubusercontent.com/Fuzss/modresources/main/maven/"
        }
    }
}
```
3. Add the following line to all `build.gradle` files of all your sub-projects (forge, fabric, common).
Replace `<LOADER>` with the specific loader (e.g. `forge`) and use `fabric` in your common project.
```groovy    
dependencies {
    modApi("de.mrjulsen.dragnsoundsapi:dragnsounds-<LOADER>:<MINECRAFT_VERSION>-<DRAGNSOUNDS_VERSION>")
}
```
4. Add the dependencies to your mod.
In the `forge` project, add this to your `mods.toml` file:
```toml
[[dependencies.<YOUR_MODID>]]
modId="dragnsounds"
mandatory=true
versionRange="[<MINECRAFT_VERSION>-<DRAGNSOUNDS_VERSION>,)"
ordering="NONE"
side="BOTH"
```
... and in the `fabric` project, add this to your `fabric.mod.json` file:
```json
"depends": {
    "dragnsounds": ">=<MINECRAFT_VERSION>-<DRAGNSOUNDS_VERSION>"
},
```
5. Reload your project.

