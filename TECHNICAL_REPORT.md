# Technical report

## Scope and outcome

This audit covers the Integrated Scripting compatibility surface in All the
Mons 1.2.0. It does not select a complete 1.2.0 knowledge snapshot and does not
claim an in-game GraalVM terminal test.

The 1.0.22-only Mixin cannot be carried forward. Integrated Scripting 1.0.24
removes its relocated Truffle classes from the mod JAR and replaces them with
unrelocated Graal/Truffle 25.0.1 dependencies loaded at runtime through a
fallback classloader. Version 1.1.0 of this project therefore applies no Mixin
and serves only as a removable transition artifact.

## Exact pack and artifact evidence

| Evidence | Exact identity |
| --- | --- |
| All the Mons export | CurseForge project `1356598`, file `8572588`, `All the Mons-1.2.0.zip` |
| Export size | `83,490,444` bytes |
| Export SHA-256 | `1d37df201daddecf5454115f5205cca15ca6ab84ed102bcc2e312f4c14876e5d` |
| Export manifest | 402 file records; Minecraft `1.21.1`; NeoForge `21.1.248` |
| Integrated Scripting manifest record | CurseForge project `889785`, file `8567360` |
| Exact runtime filename | `integratedscripting-1.21.1-neoforge-1.0.24-424.jar` |
| Exact runtime size | `27,892,500` bytes |
| Exact runtime SHA-1 | `fa8cf106196f2a364895cb591994230c04bded87` |
| Exact runtime SHA-256 | `10cafdeeece71175741f8b6c405c1bbfca09c7dfff416b565b87720af4f40545` |

The exact JAR's `META-INF/neoforge.mods.toml` declares Integrated Scripting
`1.0.24`. Its manifest records build time `2026-08-03T05:49:54+0000`.

CurseForge and Modrinth independently published build 424. Their archives have
different manifests because the publication timestamps differ by 19 seconds,
but every other extracted file has identical SHA-256 content. This is
distribution corroboration, not a claim that the two complete ZIP byte streams
are identical.

## Exact source correlation

The filename suffix `424`, artifact timestamp, publication time, and GitHub
Actions run number correlate the shipped build to upstream commit
`1f523603b32d916825afb1a7d7352bbf0f5e171d` on `master-1.21-lts`.
[Upstream CI run 424](https://github.com/CyclopsMC/IntegratedScripting/actions/runs/30788154346)
completed successfully and published the CurseForge artifact from its Java 21
Microsoft job.

The version tag `1.21.1-1.0.24` resolves to source commit
`d628b1906d57426ced9ba6da92018db76cf26fc3`; build 424 adds only later fixes and
CI changes through `1f523603`. The packaging rewrite entered at commit
[`f44ee323ab41b71ec8bd08dbf819b06167713755`](https://github.com/CyclopsMC/IntegratedScripting/commit/f44ee323ab41b71ec8bd08dbf819b06167713755).
The upstream 1.0.24 changelog calls it “Rewrite shadowing graal using fallback
classloader” and closes the GraalVM crash report
[#63](https://github.com/CyclopsMC/IntegratedScripting/issues/63).

This correlation is source evidence for the loader design. The exact runtime
JAR inspection below remains the controlling evidence for class presence and
bytecode.

## Packaging and bytecode audit

The 1.0.24 outer JAR contains neither of the historical relocated classes:

```text
org.cyclops.integratedscripting.vendors.com.oracle.truffle.runtime.hotspot.HotSpotTruffleRuntimeAccess
org.cyclops.integratedscripting.vendors.com.oracle.truffle.api.impl.DefaultTruffleRuntime
```

It instead contains 13 nested JARs under `META-INF/packageddependencies/`,
including `truffle-runtime-25.0.1.jar` and `truffle-api-25.0.1.jar`. The nested
runtime service descriptor selects:

```text
com.oracle.truffle.runtime.hotspot.HotSpotTruffleRuntimeAccess
```

The analogous exact members still exist inside those packaged dependencies:

```text
HotSpotTruffleRuntimeAccess.createRuntime()
  ()Lcom/oracle/truffle/api/TruffleRuntime;

DefaultTruffleRuntime()
  ()V

DefaultTruffleRuntime(String)
  (Ljava/lang/String;)V
```

`createRuntime()` still begins by calling `ModulesSupport.exportJVMCI` and can
select libgraal or a host compiler module. The relevant difference is package
and classloader architecture, not the disappearance of optimized Truffle.

`PackagedDependenciesLoader.load()` extracts the 13 JARs into the game-local
`libraries-integratedscripting` directory, adds them to NeoForge's fallback
classloader via `UnsafeHelper`, temporarily installs that loader as the thread
context classloader, and preloads a JavaScript context. This happens from the
Integrated Scripting mod constructor.

## Why the old Mixin was removed

The 1.0.22 Mixin targets a class name that is absent in 1.0.24. Leaving it
enabled would not protect the client. Mixin 0.8.7 can log and skip a missing
target during configuration; the injector's `require = 1` is checked only if
the Mixin reaches a transformed target. The historical “fail loudly” intent
therefore does not cover this absent-class packaging change.

Changing only its target string to the new unrelocated class would not be a
valid port. The new target is not an outer mod-JAR class presented to the
ordinary ModLauncher/Mixin transformation pipeline; it is loaded later from
runtime-extracted dependencies by a fallback `URLClassLoader`. A Mixin config
cannot be assumed to transform that separate load path.

The upstream rewrite also directly addresses the old root cause: Graal/Truffle
class names are no longer partially relocated away from the package and native
symbol names expected by GraalVM. Adding a second interception layer without a
new observed failure would widen risk and override upstream behavior without
evidence.

## Verification boundary

Observed for this project version:

- exact export and Integrated Scripting artifact identities verified;
- exact outer and packaged-dependency class paths and descriptors inspected;
- build 424 correlated to upstream source and successful upstream CI;
- local `./gradlew --no-daemon clean check build` passed with OpenJDK
  `21.0.12` and NeoForge `21.1.248`;
- two clean `check build` plus publication-POM runs passed with GraalVM
  Community `21.0.2+13.1`, the JVM used for the original 1.0.22 fix;
- `generatePomFileForModPublication` passed and produced version `1.1.0`
  coordinates;
- archive tasks disable file timestamps and use reproducible entry ordering;
  both final clean GraalVM builds produced the same 1,966-byte JAR with SHA-256
  `d1c79501ed528ab453b6ef5e8d8e208b0684c48603a389f626c9722784f1537d`;
- the production JAR contains only expanded mod metadata and this project's
  entry-point class. It contains no Mixin config/classes, nested JARs,
  Integrated Scripting or Graal/Truffle classes, or research/runtime output.

Upstream run 424 built and ran its game-test server matrix on Java 21–23 from
Microsoft, Oracle, Adoptium, and Temurin distributions. It did not test
GraalVM. This maintenance task also did not run a full All the Mons client,
open a Scripting Terminal, connect to multiplayer, or execute scripts on
GraalVM. Those remain staging acceptance gates and must not be inferred from
the static audit or successful builds.

For All the Mons 1.2.0, removing this helper is the preferred deployment. If
version 1.1.0 is temporarily installed for upgrade continuity, its expected
behavior is limited to one inactive-transition log message and no Mixin.
