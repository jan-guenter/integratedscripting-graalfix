# AGENTS.md

## Purpose

This repository builds a client-only NeoForge 1.21.1 compatibility mod for
Integrated Scripting 1.0.22 on GraalVM Community JDK 21.0.2. The patch must
remain narrowly scoped to Integrated Scripting's relocated Truffle runtime.

## Non-negotiable constraints

- Never modify a user's Minecraft or PrismLauncher installation. Reading its
  exact mod JAR and logs is allowed when the user explicitly supplies a path.
- Do not package or redistribute the Integrated Scripting JAR.
- Do not globally disable GraalVM, JVMCI, or libgraal.
- Do not set the JVM-global `truffle.TruffleRuntime` property.
- Keep the mod client-only with no content, registries, packets, or protocol
  changes. Servers must not require it.
- Keep the Integrated Scripting dependency locked to `1.0.22` until another
  version's bytecode and behavior have been inspected.
- Relocated third-party mixin targets must use string targets and
  `remap = false`. Required injections must fail clearly if the target changes.
- Avoid references that initialize relocated Truffle classes before the mixin
  executes.

## Current implementation

`HotSpotTruffleRuntimeAccessMixin` injects at the first instruction of the
relocated static `HotSpotTruffleRuntimeAccess.createRuntime()` method and
returns the embedded `DefaultTruffleRuntime(String)`. This happens before the
embedded runtime initializes JVMCI, tests libgraal, or discovers the host Graal
compiler module. Minecraft's own JVMCI compiler remains untouched.

Do not reduce the patch to `LibGraal.isAvailable() == false`: the documented
`-XX:-UseJVMCINativeLibrary` test proved that the fallback host compiler-module
path also crashes because embedded package names are relocated.

See `TECHNICAL_REPORT.md` for bytecode offsets and the two observed failures.

## Development workflow

- Use Java 21, preferably GraalVM Community JDK 21.0.2.
- Build and verify with `./gradlew clean check build` (or `gradlew.bat` on
  Windows).
- Do not commit `.gradle/`, `build/`, `dist/`, game runs, logs, crash reports,
  or third-party mod JARs.
- Keep source formatted consistently and make `checkstyleMain` pass.
- Inspect the final JAR to confirm it contains mod metadata, the mixin config,
  and only this project's classes/resources.
- For behavior changes, test startup, terminal open/create/edit/save/evaluate,
  terminal reopen, multiplayer reconnect, and absence of new Truffle/libgraal
  errors. Compare FPS against the unpatched GraalVM baseline when relevant.

## Releases

Version tags matching `v*` trigger `.github/workflows/release.yml`. The tag must
exactly equal `v` followed by `mod_version`. The workflow runs checks, builds
the JAR, publishes its Maven coordinates to GitHub Packages, creates SHA-256
checksums, and publishes the JAR and checksums as a GitHub release.

Every pull request must increase `mod_version` above the value on its target
branch. The required `Version increment` check enforces this policy and leaves
an explanatory PR comment when it fails. Update the version, documentation,
and compatibility ranges intentionally before merging and tagging.

## Evidence from the first successful test

The mod was built with GraalVM Community OpenJDK 21.0.2+13.1 and tested in All
the Mons 1.0.1 with NeoForge 21.1.234 and Integrated Scripting build 373. The
mixin applied once, the activation message appeared once, scripts worked, no
new crash report was generated, and no related Truffle, JNI, JVMCI, or mixin
errors appeared afterward.
