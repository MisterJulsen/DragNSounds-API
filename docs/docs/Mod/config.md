# Config
The mod also provides some config options which can be changed.

!!! warning
    Please note that changes to the library's config affect ALL addons that use this api!

## Client

| Option | Description | Allowed Values | Default |
| - | - | - | - |
| `max_streaming_channels` | The amount of streaming sound channels. By default, Minecraft has 8 streaming channels, but this mod increases it to 32. (Restart required) | `8 - 255` | `32` |

## Common

| Option | Description | Allowed Values | Default |
| - | - | - | - |
| `permission.sound_command_usage` | Minimum [permission level](https://minecraft.wiki/w/Permission_level) required to use the basic features of the [/sound](../Mod/commands.md/#sound) command, such as playing, stopping and modifying sounds. | `0 - 4` | `2` |
| `permission.sound_command_management` | Minimum [permission level](https://minecraft.wiki/w/Permission_level) required to use all features of the [/sound](../Mod/commands.md/#sound) command, such as uploading and deleting sound files. | `0 - 4` | `3` |
| `cleanup_on_server_start` | If active, a file cleanup will be performed at server startup to clean up unreachable ('dead') files or empty folders. | Boolean | `true` |