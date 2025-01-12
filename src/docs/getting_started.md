---
hide:
  - navigation
---
# Getting started
To start using the library in your mod, follow the steps below.
You can find all available versions on CurseForge or Modrinth.

## Architectury Loom setup
1. Create a new [Architectury project](https://docs.architectury.dev/plugin/get_started).
2. Add the following content into the `build.gradle` file of your root project.
```groovy
subprojects {
    repositories {
        maven { url = "https://raw.githubusercontent.com/MisterJulsen/modsrepo/main/maven" } // DragonLib, DragNSounds API
        maven { url = "https://raw.githubusercontent.com/Fuzss/modresources/main/maven/" } // Forge Config API
        maven { url = "https://mvnrepository.com/artifact/ws.schild/jave-all-deps"} // JAVE
    }
}
```
3. Add the following content to all `build.gradle` files of all your sub-projects (forge, fabric, common).
Replace `<LOADER>` with the specific loader (e.g. `forge`) and use `fabric` in your common project.
```groovy    
dependencies {
    modCompileOnly("dev.architectury:architectury-<LOADER>:<ARCHITECTURY_VERSION>")
    modImplementation("de.mrjulsen.mcdragonlib:dragonlib-<LOADER>:<MINECRAFT_VERSION>-<DRAGONLIB_VERSION>")
    modImplementation("de.mrjulsen.dragnsounds:dragnsounds-<LOADER>:<MINECRAFT_VERSION>-<DRAGNSOUNDS_VERSION>")

    modImplementation("ws.schild:jave-core:3.5.0")
    modImplementation("ws.schild:jave-nativebin-linux32:3.5.0")
    modImplementation("ws.schild:jave-nativebin-linux64:3.5.0")
    modImplementation("ws.schild:jave-nativebin-linux-arm32:3.5.0")
    modImplementation("ws.schild:jave-nativebin-linux-arm64:3.5.0")
    modImplementation("ws.schild:jave-nativebin-win32:3.5.0")
    modImplementation("ws.schild:jave-nativebin-win64:3.5.0")
    modImplementation("ws.schild:jave-nativebin-osxm1:3.5.0")
    modImplementation("ws.schild:jave-nativebin-osx64:3.5.0")
}
```
!!! note
    To use JAVE in Forge mods, `forgeRuntimeLibrary` must be added: `modImplementation(forgeRuntimeLibrary(...))`

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

