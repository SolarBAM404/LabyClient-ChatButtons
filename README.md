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

Use `setButtons` to replace that player's full set, `removeButton` to remove one, and `clearButtons` to remove all. The example classes show one way to expose the service from a Paper plugin. The published `chat-buttons-api` artifact contains the developer-facing types; a Paper plugin must provide the transport and service implementation at runtime.

`withColors(backgroundArgb, textArgb)` sets 32-bit ARGB colors. `withItemIcon("namespace:item")` uses a vanilla item ID rendered as the button icon; icons are decorative and do not affect the server-side command or permission checks.

## Publish the Paper API

The [Paper API publishing workflow](.github/workflows/publish-api.yml) uploads only `chat-buttons-api` under `io.gitlab.solarm404` to the Maven Central Portal. The project uses the [MIT license](LICENSE). Verify that `io.gitlab.solarm404` is registered to your Central Portal account, then generate a [Portal user token](https://central.sonatype.org/publish/generate-portal-token/) and a [GPG signing key](https://central.sonatype.org/publish/requirements/gpg/). Publish the key's public half to a key server as described in the GPG guide.

Add these four GitHub Actions repository secrets:

| Secret | Value |
| --- | --- |
| `MAVEN_CENTRAL_USERNAME` | Portal user token username |
| `MAVEN_CENTRAL_PASSWORD` | Portal user token password |
| `SIGNING_KEY` | ASCII-armored private GPG key |
| `SIGNING_PASSWORD` | Passphrase for the private key, if it has one |

Run **Publish Paper API to Maven Central** manually from the Actions tab and enter a version such as `1.0.0`. The workflow uploads a signed deployment. Review it in [Central Portal Deployments](https://central.sonatype.com/publishing/deployments), then click **Publish** there. Released versions cannot be replaced, so use a new version for each release. The Java package names stay `me.solar.laby.chatbuttons`; only the Maven group changes.

After the release is available on Maven Central, another Paper plugin can depend on it without registry credentials:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.gitlab.solarm404:chat-buttons-api:1.0.0")
}
```

The API defines types only: the example Paper plugin provides the service implementation at runtime.

## Wire messages

Server to client:

```json
{"type":"buttons","buttons":[{"id":"creative","label":"Creative","tooltip":"Switch to creative mode","backgroundColor":4281761290,"textColor":4294967295,"itemIcon":"minecraft:emerald"}]}
```

Client to server:

```json
{"type":"click","id":"creative"}
```

When the addon is turned back on while connected, it requests the current server-owned snapshot:

```json
{"type":"sync"}
```

The server accepts click requests only for IDs currently registered to the sending player. Each set/update sends a complete snapshot; an empty snapshot removes all visible buttons.
