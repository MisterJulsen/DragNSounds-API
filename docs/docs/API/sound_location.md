# Sound Location
The `SoundLocation` is similar to the `ResourceLocation`. It is used to describe the relative storage location for a sound file.

A `SoundLocation` consists of two components, both of which are theoretically optional:

- Namespace: The first folder in the library's data folder
- Path: The path structure of all further subdirectories up to the folder in which the sound file is located.

!!! tip
    It is strongly recommended to **always** specify a namespace to keep things organized. Best practice is that the namespace corresponds to the `MODID` or at least contains it if a mod uses multiple namespaces.

The `String` representation of a `SoundLocation` is then similar to `ResourceLocation` in the following format:
```txt
namespace:path/to/file
```

!!! example
    Also possible:

    - `namespace:` - The sound files are in the namespace folder.
    - `:` - The sound files are in the root folder (not recommended).
    - `namespace:/path` - The sound files are in the `namespace/path` folder. The `:/` is not neccessary, but it also doesn't matter.

## Using SoundLocation in commands
A `SoundLocation` can also be used as a command argument. The `String` notation is used there. It is important to note that although only existing `SoundLocations` are suggested, you can also create your own location in a command.

To use a `SoundLocation` in your own command you can use the [SoundLocationArgument](../javadoc/de/mrjulsen/dragnsounds/commands/arguments/SoundLocationArgument.html).