# Commands
This library not only provides an API for other projects, but with the built-in commands it can also be used as an independent mod with the basic features. This can be particularly interesting for data pack creators and server operators.

## /sound
The sound command and its sub-commands can be used to use the basic features of the library in Minecraft itself or via data packs.
### `play`
```mcfunction
/sound play
    <file>
    [<targets>]
    [<source>]
    [<volume>]
    [<pitch>]
    [<position>]
    [<attenuationDistance>]
    [<ticksOffset>]
    [<showLabel>]
```

**Description**

Plays a specific sound for the specified amount of players, given the specified arguments.

**Arguments**

| Argument | Description | Default | Type |
| -------- | -------- | -------- | -------- |
| `file` | The sound file that should be played. |  | [SoundFile](../API/sound_file.md/#using-soundfile-in-commands) |
| `targets` | Optional: Players for whom the sound should be played. | `@s` | [EntitySelector](https://minecraft.wiki/w/Target_selectors) |
| `source` | Optional: The sound source in which the sound should be played in. | `CUSTOM` | Enum |
| `volume` | Optional: The volume of the sound. Must be a value between `0.0` and `1.0`. | `1.0` | Float |
| `pitch` | Optional: The pitch of the sound. Must be a value between `0.5` and `2.0`.| `1.0` | Float |
| `position` | Optional: The position of the sound in the world. Only works for `Mono` sounds! | `~ ~ ~` | Vec3 |
| `attenuationDistance` | Optional: The attenuation distance of the sound in blocks. | `16` | Integer |
| `ticksOffset` | Optional: Fast forwards the sound the given number of ticks before playing. Value must be larger than 0. | `0` | Integer |
| `showLabel` | If `true`, a text will be displayed above the players' hotbar (like records) | `false` | Boolean |

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server

### `stop`
```mcfunction
/sound stop
    [<targets>]
    [<file>]
```

**Description**

Stops all playing instances of one or all sounds for the selected players.

**Arguments**

| Argument | Description | Default | Type |
| -------- | -------- | -------- | -------- |
| `targets` | Optional: Players for whom the sound should be played. | `@s` | [EntitySelector](https://minecraft.wiki/w/Target_selectors) |
| `file` | Optional: The sound file that should be stopped. | all | [SoundFile](../API/sound_file.md/#using-soundfile-in-commands) |

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server

### `upload`
```mcfunction
/sound upload
    <location>
    <displayName>
    [<target>]
    [<channels> <bitRate> <samplingRate> <quality>]
    [<showProgress>]
```

**Description**

Opens a file picker dialog for the selected player where it can choose one sound file it want to upload.

**Arguments**

| Argument | Description | Default | Type |
| -------- | -------- | -------- | -------- |
| `file` | The location where the sound should be stored. You can also create a new location. |  | [SoundLocation](../API/sound_location.md/#using-soundlocation-in-commands) |
| `displayName` | A human-readable name for the sound which is displayed when playing the sound. |  | String |
| `target` | Optional: One player who should upoad the sound. | `@s` | [EntitySelector](https://minecraft.wiki/w/Target_selectors) |
| `channels` | Optional: The audio channels the converted sound should have. | (source) | Enum |
| `bitRate` | Optional: The bit rate the converted sound should have. | (source) | Integer |
| `samplingRate` | Optional: The sampling rate the converted sound should have. | (source) | Integer |
| `quality` | Optional: The quality the converted sound should have. Must be a value between 0 and 10. | `5` | Byte |
| `showProgress` | Optional: When `true` the player will see an upload progress screen with a cancel option while uploading. | `true` | Boolean |

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

3

**Environment**

Client, Server

### `delete`
```mcfunction
/sound delete
    <file>
```

**Description**

Deletes the selected file from the server. Sound instances of that file that are currently playing will continue playing.

**Arguments**

| Argument | Description | Default | Type |
| -------- | -------- | -------- | -------- |
| `file` | The sound file which should be deleted. |  | [SoundFile](../API/sound_file.md/#using-soundfile-in-commands) |

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

3

**Environment**

Client, Server

### `cleanUp`
```mcfunction
/sound cleanUp
```

**Description**

Runs a clean up which will removed all unused sound files and empty sound locations. This action will be performed every time when the server starts.

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

3

**Environment**

Client, Server

### `showFolder`
```mcfunction
/sound showFolder
```

**Description**

Opens the folder where the sounds are stored.

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client

### `help`
```mcfunction
/sound help
```

**Description**

Shows this documentation.

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server

### `modify`
```mcfunction
/sound modify
    <target>
    <file>
    <volume|pitch|attenuationDistance|doppler|cone|pos|pause|resume|seek>
    ...
```

**Description**

Modifies the playback sound for the selected players.

**Arguments**

| Argument | Description | Default | Type |
| -------- | -------- | -------- | -------- |
| `target` | The player for whom the sound should be modified. | `@s` | [EntitySelector](https://minecraft.wiki/w/Target_selectors) |
| `file` | The sound file that should be modified. |  | [SoundFile](../API/sound_file.md/#using-soundfile-in-commands) |

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server

#### `modify volume`
```mcfunction
/sound modify <target> <file> volume
    <volume>
```

**Description**

Modifies the volume of the sound.

**Arguments**

| Argument | Description | Default | Type |
| -------- | -------- | -------- | -------- |
| `volume` | The new volume of the sound. |  | Float |

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server

#### `modify pitch`
```mcfunction
/sound modify <target> <file> pitch
    <pitch>
```

**Description**

Modifies the pitch of the sound.

**Arguments**

| Argument | Description | Default | Type |
| -------- | -------- | -------- | -------- |
| `pitch` | The new pitch of the sound. |  | Float |

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server

#### `modify attenuationDistance`
```mcfunction
/sound modify <target> <file> attenuationDistance
    <distance>
```

**Description**

Modifies the attenuation distance.

**Arguments**

| Argument | Description | Default | Type |
| -------- | -------- | -------- | -------- |
| `attenuationDistance` | The new attenuation distance of the sound. |  | Integer |

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server

#### `modify doppler`
```mcfunction
/sound modify <target> <file> doppler
    <velocity>
    <dopplerFactor>
```

**Description**

Enables the doppler effect for the sound.

**Arguments**

| Argument | Description | Default | Type |
| -------- | -------- | -------- | -------- |
| `velocity` | The velocity of the sound source. |  | Vec3 |
| `dopplerFactor` | The doppler factor value. |  | Float |

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server

#### `modify cone`
```mcfunction
/sound modify <target> <file> cone
    <direction>
    <angleA>
    <angleB>
    [<outerGain>]
```

**Description**

Defines a cone area in which the sound can be heard. Useful to simulate directional speakers.

**Arguments**

| Argument | Description | Default | Type |
| -------- | -------- | -------- | -------- |
| `direction` | The direction of the sound source. |  | Vec3 |
| `angleA` | The first angle of the cone area. |  | Float |
| `angleB` | The second angle of the cone area. |  | Float |
| `outerGain` | Optional: The volume of the sound outside the defined bounds. Must be a value between `0.0` and `1.0`. | `1.0` | Float |

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server

#### `modify pos`
```mcfunction
/sound modify <target> <file> pos
    <position>
```

**Description**

Changes the position of the sound source in the world.

**Arguments**

| Argument | Description | Default | Type |
| -------- | -------- | -------- | -------- |
| `pos` | The new position of the sound. |  | Vec3 |

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server

#### `modify pause`
```mcfunction
/sound modify <target> <file> pause
```

**Description**

Pauses the playback of the selected sound for the selected player.

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server

#### `modify resume`
```mcfunction
/sound modify <target> <file> resume
```

**Description**

Resumes the playback of the selected sound for the selected player.

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server

#### `modify seek`
```mcfunction
/sound modify <target> <file> seek
    <ticks>
```

**Description**

Skips the given amount of ticks in the sound.

**Arguments**

| Argument | Description | Default | Type |
| -------- | -------- | -------- | -------- |
| `ticks` | The amount if time in ticks to skip. |  | Integer |

**[Premission level](https://minecraft.wiki/w/Permission_level) required**

2

**Environment**

Client, Server