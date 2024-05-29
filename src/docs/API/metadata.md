# Custom Metadata
If you build your own mod with this library, you may want to store additional data with the uploaded sounds (e.g. for access management). Therefore you can use the metadata slot in the [IndexFile](./index_file.md).

## Add your own metadata
There are several ways to add your own metadata to a file, but they are all very simple:

- `updateMetadata` in `SoundFile` allows you to add or update metadata while a file already exists. Key-value pairs are passed and then saved.
- When creating a new sound file (usually while uploading), metadata can be added.

## Read metadata
To read metadata you can use the `getMetadataSafe` or `getMetadata` method (to get all the metadata) in `SoundFile`. Pass the key of the data you want. If available, you will then receive a `String` with your data.

## Remove custom metadata
Metadata can also be removed at any time. To do this, use the `removeMetadata` method and pass a `Set<String>` with all the keys you want to remove. 
!!! warning
    You CANNOT remove the built-in metadata!