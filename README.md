# IBM RPG — IntelliJ Plugin

Language support for IBM i RPG / ILE RPG in IntelliJ-based IDEs. Mostly here for the syntax highlighting.

> First vibe-coded project, so be nice :)

## Features

- **Syntax highlighting**
- **Embedded SQL highlighting**
- **Autocomplete** for BIFs, opcodes/keywords, and procedures, subprocedures, subroutines, and `DCL-F` files declared in the current file.
- **Column-position ruler** — a sticky ruler across the top of RPG editors.

## Building

```sh
./gradlew buildPlugin
```

The packaged plugin lands in `build/distributions/` as a `.zip`. Install it via
**Settings → Plugins → ⚙ → Install Plugin from Disk…** in your IDE.

Or just use the included `rpg-plugin-0.1.0.zip` file.

## Requirements

**To build:**

- JDK 21
- IntelliJ 2024.3
- Gradle 9.5.1 (bundled via the included wrapper — no separate install needed)

**To use:** 

- IntelliJ 2024.3 or later.
