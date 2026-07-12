# Technical report

## Exact artifact inspected

`integratedscripting-1.21.1-neoforge-1.0.22-373.jar`, SHA-256 recorded
in the release package checksums. The installed artifact is multi-release. Its
Java 21 override is limited to the relocated `JDKAccessor`; the runtime access
class patched here is the base `HotSpotTruffleRuntimeAccess.class`.

The relocated service descriptor
`META-INF/services/org.cyclops.integratedscripting.vendors.com.oracle.truffle.api.TruffleRuntimeAccess`
names `org.cyclops.integratedscripting.vendors.com.oracle.truffle.runtime.hotspot.HotSpotTruffleRuntimeAccess`.

## Bytecode evidence

`javap -p -c` shows that `createRuntime()`:

1. calls `ModulesSupport.exportJVMCI` at bytecode offset 2;
2. initializes JVMCI at offset 19;
3. calls relocated `LibGraal.isAvailable()` at offset 259;
4. constructs `LibGraalTruffleCompilationSupport` when true;
5. when false and a host compiler module exists, calls
   `getTruffleGraalHotSpotPackage(Module)` at offset 521;
6. otherwise returns the bundled `DefaultTruffleRuntime` at offset 510.

The original failure took branch 4 and reached relocated native declarations
whose JNI symbols are absent. The 2026-07-12 03:19:29 crash generated with
`-XX:-UseJVMCINativeLibrary` took branch 5 and failed with:

`Cannot find package containing Truffle runtime in jdk.internal.vm.compiler module`

The names searched were the host Graal package and Integrated Scripting's
relocated embedded package. This establishes that forcing only
`LibGraal.isAvailable()` false would still crash on this GraalVM.

## Patch

The mixin injects at `HEAD` of the exact static `createRuntime()` method,
cancels it, and returns the embedded `DefaultTruffleRuntime(String)`. It uses
string class names and reflection so loading the compatibility mod does not
initialize the target Truffle classes early. `require = 1` and a required mixin
configuration make changed targets fail explicitly.

This happens before any JVMCI or libgraal call in the embedded runtime. It does
not modify VM options or system properties, so Minecraft's host Graal compiler
is unaffected.

## Verification completed

- Built successfully against NeoForge 21.1.234 with GraalVM Community
  OpenJDK 21.0.2+13.1 (`21.0.2+13-jvmci-23.1-b30`).
- Verified the built JAR contains its mod metadata, required mixin config,
  mixin class, and manifest `MixinConfigs` entry.
- No original Integrated Scripting classes or JAR are packaged.

In-game terminal behavior and FPS require user testing in the existing pack.
