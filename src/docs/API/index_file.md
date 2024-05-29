# Index File
The index file (`index.nbt`) contains all information about all sound files in the same [SoundLocation](./sound_location.md) directory. Only sound files registered in the index file can be used. Unknown files are usually cleaned up when the server starts.

## Structure

```txt
> root
|   > Files
|   |   > sound_id_1
|   |   |   > Info
|   |   |   |   > Artist
|   |   |   |   > Album
|   |   |   |   > Title
|   |   |   |   > Channels
|   |   |   |   > Duration
|   |   |   |   > Genre
|   |   |   |   > Date
|   |   |   |   > UploadTime
|   |   |   |   > Size
|   |   |   |   > Owner
|   |   |   > Location
|   |   |   |   > Namespace
|   |   |   |   > Path
|   |   |   > Metadata
|   |   |   |   > DisplayName
|   |   |   |   > ...
|   |   > sound_id_2
|   |   > ...
|   > Version
```

## Manipulation
The file can be edited with any NBT editor. This means that you can theoretically delete registered sound files, adjust their data, force the information to be updated by removing the hash value or even insert new entries. However, this is **not recommended** as this can cause many problems when done incorrectly. It is best to use add-ons or commands for this.