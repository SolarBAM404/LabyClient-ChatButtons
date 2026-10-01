# LabyMod Chat Buttons

This project contains a Paper-facing API and a small example plugin for server-owned chat buttons. It is intended to pair with a LabyMod 4 addon that renders buttons beneath the chat input and sends a button ID back to the server.

## Current status

- `chat-buttons-api` provides the developer-facing `ChatButton` model and `ChatButtonService` interface.
- `paper-example` contains the Paper transport/service implementation and a `/chatbuttons show|remove` demo.
- `client-addon` is a LabyMod 4 addon targeting Minecraft 26.2. It renders the button row and sends click IDs using the `chatbuttons:main` plugin-message channel.

The Paper implementation uses a namespaced plugin-message channel (`chatbuttons:main`) with small JSON messages. Button commands are never sent to the client. The client can request only a button ID; Paper looks up the current server-owned button, checks permission, and dispatches the configured command as that player.

The client addon is version-specific and currently targets Minecraft 26.2. It uses LabyMod's official addon template/Gradle plugins plus versioned mixins for the chat UI and incoming plugin-message payload. The addon is separate from the Paper Gradle build because LabyMod has its own Gradle plugin build setup.

## Build

Java sources follow [Google Java Style](https://google.github.io/styleguide/javaguide.html), enforced with Google Java Format through Spotless.

Format all Paper API/plugin Java sources:

```sh
./gradlew spotlessApply
```

Format the separate LabyMod addon build:

```sh
./gradlew -p client-addon spotlessApply
```

Run `spotlessCheck` in each build to verify formatting without changing files.

Use JDK 25. Build the Paper demo and API with:

```sh
./gradlew :paper-example:build
```

Output: `paper-example/build/libs/paper-example-1.0.0-SNAPSHOT.jar`.

Build the LabyMod addon from its nested project:

```sh
./gradlew -p client-addon build
./gradlew -p client-addon createReleaseJar
```

The release jar is produced in `client-addon/build/libs/`.

## Run the local Paper demo

1. Download a Paper 26.2 server jar from the [Paper downloads page](https://papermc.io/downloads/paper) into `run/paper.jar`.
2. Build this project, then copy `paper-example/build/libs/paper-example-1.0.0-SNAPSHOT.jar` to `run/plugins/`.
3. Start Paper once, accept the EULA in `run/eula.txt`, and restart the server.
4. Join as an operator using LabyMod 4 and run `/chatbuttons show`. Grant `chatbuttons.example.creative` to a non-operator to try the permission path. Run `/chatbuttons remove` to clear the buttons.

Install the release jar in LabyMod 4's addon folder. The demo's button state will then render along the bottom of the open chat screen.

## API usage

The service is exposed by `ChatButtonsPlugin#chatButtons()` in the demo plugin. Other plugins can register buttons for a `Player`:

```java
service.addButton(player, new ChatButton(
    "creative", "Creative", "Switch to creative mode",
    "example.creative", "gamemode creative"
));

service.addButton(player, new ChatButton(
    "heal", "Heal", "Restore health", null,
    "effect give @s minecraft:instant_health 1 1 true"
).withColors(0xFFC62828, 0xFFFFFFFF).withItemIcon("minecraft:golden_apple"));
```

Use `setButtons` to replace that player's full set, `removeButton` to remove one, and `clearButtons` to remove all. The example classes show one way to expose the service from a Paper plugin. A production API distribution should publish `chat-buttons-api` as a separate artifact and expose the transport as a required service/plugin dependency.

`withColors(backgroundArgb, textArgb)` sets 32-bit ARGB colors. `withItemIcon("namespace:item")` uses a vanilla item ID rendered as the button icon; icons are decorative and do not affect the server-side command or permission checks.

## Wire messages

Server to client:

```json
{"type":"buttons","buttons":[{"id":"creative","label":"Creative","tooltip":"Switch to creative mode","backgroundColor":4281761290,"textColor":4294967295,"itemIcon":"minecraft:emerald"}]}
```

Client to server:

```json
{"type":"click","id":"creative"}
```

The server accepts click requests only for IDs currently registered to the sending player. Each set/update sends a complete snapshot; an empty snapshot removes all visible buttons.
