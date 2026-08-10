# Changelog

## 1.1.0

- Retargeted project metadata to All the Mons 1.2.0, NeoForge 21.1.248, and
  pack build 424 of Integrated Scripting (metadata version 1.0.24).
- Removed the 1.0.22-only Mixin because its relocated target is absent from the
  new fallback-classloader packaging architecture.
- Converted the mod into a client-only transition helper that logs that it is
  inactive and can be removed.
- Recorded the exact pack, artifact, source, bytecode, and validation boundary
  in `TECHNICAL_REPORT.md`.

## 1.0.1

- Published the 1.0.22 patch through GitHub Releases and GitHub Packages.

## 1.0.0

- Added the client-only interpreter fallback Mixin for Integrated Scripting
  1.0.22 on GraalVM Community JDK 21.0.2.
