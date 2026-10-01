# Chat Buttons LabyMod addon

LabyMod 4 addon targeting Minecraft 26.2. It accepts the Paper demo's JSON button snapshot on `chatbuttons:main`, draws buttons above the chat input, supports optional hover tooltips, ARGB text/background colors, and vanilla item icons, and sends only the clicked button ID back on that same channel. Command strings and permission values are never stored or received by the client. Button labels and tooltips are supplied by server plugins at runtime, so servers can provide text appropriate to their players' language.

Build from the repository root with:

```sh
./gradlew -p client-addon build
./gradlew -p client-addon createReleaseJar
```

From the repository root, `./gradlew build` and `./gradlew createReleaseJar` also include this addon build for CI.

Install the resulting `*-release.jar` from `client-addon/build/libs/` in the LabyMod 4 addon directory. This first implementation is versioned for Minecraft 26.2; other Minecraft versions need corresponding versioned mixins.
