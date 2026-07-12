# Integrated Scripting GraalVM Fix

A local, client-only NeoForge 1.21.1 compatibility mod for exactly Integrated
Scripting 1.0.22. It prevents the scripting terminal from initializing the
mod's relocated native/host Graal compiler paths and selects the interpreter
runtime already bundled inside Integrated Scripting.

The patch does **not** change JVM flags, JVMCI, libgraal, or Graal compilation
for Minecraft and other mods. Only Integrated Scripting's shaded Truffle
runtime is intercepted.

## Install

1. Remove the attempted `-Dtruffle.TruffleRuntime=...` and
   `-XX:-UseJVMCINativeLibrary` workarounds from the instance's JVM arguments.
2. Add `integratedscripting_graalfix-1.0.0.jar` to the instance through
   PrismLauncher's **Mods > Add file** action.
3. Leave the original Integrated Scripting JAR installed.

The server does not need this mod. It adds no content, packets, or registries.
Remove its JAR to uninstall it.

## Build

Use GraalVM Community JDK 21.0.2 and the included Gradle wrapper:

```powershell
$env:JAVA_HOME = 'C:\Program Files\GraalVM\graalvm-community-openjdk-21.0.2+13.1'
.\gradlew.bat clean build
```

The output is under `build/libs/`.

## Technical note

In build 373, the service provider is the relocated
`HotSpotTruffleRuntimeAccess`. Its `createRuntime()` first tests relocated
libgraal and otherwise attempts to load compiler support from the host
`jdk.graal.compiler` module. On GraalVM 21.0.2 the first path reaches relocated
JNI declarations whose symbols do not exist. With the native library disabled,
the second path cannot find the package names expected by the embedded Truffle
25.0.1 classes.

The required mixin cancels `createRuntime()` at its first instruction and
returns the embedded `DefaultTruffleRuntime`. Injection uses `require = 1`, so
a changed target fails clearly instead of silently losing the patch.

## Expected performance impact

Integrated Scripting JavaScript runs in interpreter mode. Minecraft's own
GraalVM compiler remains enabled. Script-heavy Integrated Dynamics networks
may evaluate more slowly than they would with a compatible optimized Truffle
runtime.
