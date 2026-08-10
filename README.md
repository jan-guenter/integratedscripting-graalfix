# Integrated Scripting GraalVM Fix

A client-side transition helper for Minecraft 1.21.1, NeoForge 21.1.248, and
Integrated Scripting 1.0.24 as shipped by All the Mons 1.2.0.

Integrated Scripting 1.0.22 bundled a relocated Truffle/GraalJS runtime that
could select native or host Graal compiler integration incompatible with its
relocated package names. Releases 1.0.0 and 1.0.1 of this project intercepted
that runtime selection and forced Integrated Scripting's embedded interpreter.

Integrated Scripting 1.0.24 replaces the relocated runtime with upstream
Graal/Truffle 25.0.1 JARs loaded through a fallback classloader. The old Mixin
target no longer exists, and applying or retargeting that Mixin would be
incorrect. Version 1.1.0 therefore applies no Mixin. It exists so clients can
upgrade the old helper alongside All the Mons without retaining an inapplicable
patch, and it logs that the helper can now be removed.

## Compatibility

- All the Mons 1.2.0
- Minecraft 1.21.1
- NeoForge 21.1.248 through 21.1.x
- Integrated Scripting 1.0.24 (audited pack build 424)
- Java 21

The mod remains client-side only. It adds no content, registries, packets, or
network protocol changes, and it is not required on a server.

NeoForge's dependency range can enforce `1.0.24`, but it cannot distinguish
provider build numbers that expose the same mod version. The exact audited All
the Mons artifact is build 424; other 1.0.24 builds are not covered by the
bytecode audit.

## Upgrading from 1.0.1

The recommended action for All the Mons 1.2.0 is to remove
`integratedscripting_graalfix` from the client. If an automated mod-management
workflow expects an updated artifact first, install version 1.1.0; it performs
no patch and logs:

```text
Inactive for Integrated Scripting 1.0.24: its upstream fallback-classloader rewrite removed the relocated Truffle target, so no compatibility patch is applied; this transition mod can be removed
```

Do not use 1.1.0 with Integrated Scripting 1.0.22. Keep version 1.0.1 only for
the older, exact 1.0.22 runtime it was built and tested against.

## Artifacts

Tagged releases are published through
[GitHub Releases](https://github.com/jan-guenter/integratedscripting-graalfix/releases)
with SHA-256 checksums and through
[GitHub Packages](https://github.com/jan-guenter/integratedscripting-graalfix/packages).

Maven coordinates for this source version are:

```text
io.github.jan-guenter:integratedscripting_graalfix:1.1.0
```

## Building

Requirements:

- A Java 21 JDK
- Internet access for the first Gradle dependency download

Build with:

```shell
./gradlew --no-daemon clean check build
```

The built JAR is written to `build/libs/`. The final artifact must contain only
this project's entry-point class and metadata; it must not contain a Mixin
configuration, Integrated Scripting classes, Graal/Truffle classes, or nested
JARs.

See [`TECHNICAL_REPORT.md`](TECHNICAL_REPORT.md) for the exact All the Mons
1.2.0 artifact identities, bytecode audit, source correlation, and remaining
runtime-validation boundary.

## License

This project is available under the [MIT License](LICENSE).
