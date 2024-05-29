# API information
After setting up your project according to [this guide](../getting_started.md) you can get started straight away. The library works automatically and no further steps are required.

To make use of the library's features, use the classes [ClientApi](../javadoc/de/mrjulsen/dragnsounds/api/ClientApi.html) (if you are working on the client side) and [ServerApi](../javadoc/de/mrjulsen/dragnsounds/api/ServerApi.html) (if you are working on the server side). There is also the [Api](../javadoc/de/mrjulsen/dragnsounds/api/Api.html) class (for both sides), but this is rather unimportant for development. In general it can be said that everything you need is located inside the api package.

[Read the javadoc](../javadoc/index.html){ .md-button }

## Differences between client and server
Both the server and the client Api offer mostly the same methods, but with the difference that (depending on the page you are working on) the task must first be sent to the client or server to be executed.

Data exchange (if neccessary) occurs automatically. However, it makes sense to start the command on the right side. Always remember that the client plays the sounds and the server offers the sound files.

**Example:**
If a Sound Player Block starts playing a sound, it makes sense to execute the command on the server side, as it can then load the sound data straight away and send it to the clients. The clients then receive the data and can begin playing the sound. The other way around, a request would first go to the server, which would then do the same steps.