# Changelog

## [3.0.0] - 2025-09-25
### Breaking changes
- Raise the minimum supported Android SDK to 28 and require consumers to build with
  AGP 8.6.0, Gradle 8.7, and Java 17 to match the upgraded toolchain.

### Build system
- Upgrade the Gradle wrapper, adopt the plugins DSL, and align compile/target SDK
  levels and dependencies with the latest OpenSRP stack.
- Migrate publishing to the `maven-publish` plugin with central bundle helpers and
  new `publishToMavenLocal` delegation from the root project.

### Continuous integration
- Configure JitPack builds to run on JDK 17, expose the module publish task, and
  skip the unpublished reference app module during release builds.

### Bug fixes
- Fix SnakeYAML constructor usage so the library compiles against SnakeYAML 2.x
  on Java 17.

### Documentation
- Refresh the README with the current project status and guidance for releases
  and local development.
