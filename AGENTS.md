# AGENTS.md

## Purpose

This repository maintains the client-only compatibility lifecycle for the
Integrated Scripting/GraalVM issue originally patched for All the Mons.

Version 1.1.0 is a no-Mixin transition release for All the Mons 1.2.0,
NeoForge 21.1.248, and Integrated Scripting 1.0.24 build 424. Upstream replaced
the relocated Truffle runtime with packaged, unrelocated Graal/Truffle 25.0.1
JARs loaded through a fallback classloader. The historical target is absent and
must not be retargeted into that fallback loader without a new investigation.

## Non-negotiable constraints

- Never modify a user's Minecraft or PrismLauncher installation. Reading an
  exact mod JAR and logs is allowed only when the user supplies the scope.
- Do not package or redistribute Integrated Scripting or Graal/Truffle JARs.
- Do not globally disable GraalVM, JVMCI, or libgraal.
- Do not set JVM-global Truffle or polyglot properties.
- Keep the project client-only with no content, registries, packets, protocol
  changes, or server requirement.
- Keep the 1.1.x Integrated Scripting dependency locked to `1.0.24` until a
  different version's exact artifact and classloading behavior are inspected.
- Do not add a Mixin merely by changing the old class-name strings. The 1.0.24
  Truffle classes live in runtime-extracted packaged dependencies outside the
  ordinary mod JAR target surface.
- If a future runtime again needs intervention, prove the exact transformation
  and classloader boundary before implementation and make failures explicit.

## Historical implementation

Versions 1.0.0 and 1.0.1 injected at the first instruction of the relocated
static `HotSpotTruffleRuntimeAccess.createRuntime()` method from Integrated
Scripting 1.0.22 and returned its relocated `DefaultTruffleRuntime(String)`.
This avoided relocated JVMCI/libgraal paths while leaving Minecraft's JVMCI
compiler untouched.

That implementation is deliberately removed in 1.1.0. See
`TECHNICAL_REPORT.md` for the old and new exact class paths and evidence.

## Development workflow

- Use Java 21; CI uses GraalVM Community 21.0.2.
- Build and verify with `./gradlew --no-daemon clean check build`.
- Generate the publication metadata with
  `./gradlew --no-daemon generatePomFileForModPublication`.
- Keep Checkstyle passing.
- Inspect the final JAR. Reject Mixin configs/classes, Integrated Scripting or
  Graal/Truffle classes, nested JARs, game output, logs, and research evidence.
- For behavior changes, use a disposable All the Mons staging client and test
  startup plus terminal create/edit/save/evaluate/reopen and reconnect. Do not
  claim these runtime gates unless their exact evidence was retained.

## Releases

Tags matching `v*` trigger `.github/workflows/release.yml`. The tag must equal
`v` followed by `mod_version`. Pull requests must increase `mod_version` above
the target branch value.

Version 1.1.0 is a transition artifact, not proof that Integrated Scripting
1.0.24 works on every GraalVM distribution. Its evidence-backed claim is
narrower: the old relocated target is absent, upstream replaced that packaging
architecture, and this project intentionally applies no patch.
