# Integrated Scripting GraalVM Fix

A client-side compatibility mod for Minecraft 1.21.1, NeoForge 21.1.x, and
Integrated Scripting 1.0.22.

Integrated Scripting bundles and relocates a Truffle/GraalJS runtime. When the
game runs on GraalVM Community JDK 21, that embedded runtime can incorrectly
select native or host Graal compiler integration whose package names and native
bindings do not match the relocated classes. Opening the Scripting Terminal can
then crash the client.

This mod intercepts initialization of Integrated Scripting's relocated Truffle
runtime and selects the interpreter implementation already bundled with the
mod. The patch is deliberately limited to Integrated Scripting: it does not
change JVM options, disable JVMCI, or affect Graal compilation for Minecraft or
other mods.

## Compatibility

- Minecraft 1.21.1
- NeoForge 21.1.x
- Integrated Scripting 1.0.22
- Java 21

The compatibility range is intentionally narrow because the mixin targets
bytecode from this exact Integrated Scripting release. Other versions must be
inspected before the range can safely be expanded.

The mod is client-side only. It adds no game content, registries, packets, or
network protocol changes, and it is not required on a server.

## Installation

1. Download the JAR from the repository's Releases page.
2. Place the JAR in the client's `mods` directory alongside Integrated
   Scripting.
3. Start the game normally.

Keep the original Integrated Scripting JAR installed. To uninstall the fix,
remove only the `integratedscripting_graalfix` JAR.

When the patch activates, the client log contains:

```text
Active: using Integrated Scripting's embedded interpreter runtime; Minecraft JVMCI is unchanged
```

## Building

Requirements:

- A Java 21 JDK
- Internet access for the first Gradle dependency download

Build with the included Gradle wrapper:

```shell
./gradlew clean check build
```

On Windows:

```powershell
.\gradlew.bat clean check build
```

The built mod JAR is written to `build/libs/`.

`check` runs compilation, tests, and Checkstyle. Continuous integration also
validates the Gradle wrapper and uploads the built JAR as a workflow artifact.

## How it works

The required mixin injects at the beginning of the relocated
`HotSpotTruffleRuntimeAccess.createRuntime()` method. It returns Integrated
Scripting's embedded `DefaultTruffleRuntime` before that runtime can initialize
JVMCI, enter relocated libgraal native bindings, or discover an incompatible
host compiler module.

The injection is marked as required, so an incompatible target change causes a
clear startup failure instead of silently leaving the crash unpatched. See
[`TECHNICAL_REPORT.md`](TECHNICAL_REPORT.md) for the bytecode analysis and
implementation rationale.

## Performance considerations

Scripts evaluated by Integrated Scripting run through its interpreter rather
than its optimized Truffle compiler. Script-heavy networks may therefore
evaluate more slowly. GraalVM optimization of Minecraft and all unrelated code
remains enabled.

## License

This project is available under the [MIT License](LICENSE).
