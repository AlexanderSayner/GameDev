# StarCraft Lite - RTS Game

A production-ready real-time strategy game inspired by StarCraft, built with LWJGL3 and Gradle.

## Features

### Core Gameplay
- **Base Building**: Construct Command Centers, Barracks, Refineries, and Supply Depots
- **Unit Production**: Train SCVs (workers), Marines, and Tanks
- **Resource Management**: Gather minerals and vespene gas
- **Combat System**: Attack-move commands, auto-targeting, different weapon ranges
- **Supply System**: Manage your supply to build larger armies

### AI Opponent
Three difficulty levels with intelligent behavior:
- **Easy**: Slower build times, smaller army, less aggressive
- **Medium**: Balanced gameplay experience
- **Hard**: Fast builds, large army, highly aggressive

### Win Conditions
- **Victory**: Destroy the enemy Command Center
- **Defeat**: Your Command Center is destroyed

### Controls
| Action | Control |
|--------|---------|
| Move Camera | WASD or Arrow Keys |
| Select Units | Left-click drag (box select) or single click |
| Move | Right-click on destination |
| Attack-Move | Press A, then right-click |
| Attack Unit/Building | Right-click directly on target |
| Gather Resources | Right-click on mineral patches or geysers (SCVs only) |
| Build Barracks | Press B, then left-click to place |
| Build Refinery | Press R, then left-click to place |
| Build Supply Depot | Press S, then left-click to place |
| Center on Base | Spacebar |
| Cancel Action | Escape |

### UI Elements
- **Resource Panel** (top): Shows minerals, gas, and supply count
- **Minimap** (right): Shows entire battlefield with unit positions
- **Event Log** (left): Displays game events and notifications
- **Objectives Panel** (bottom-left): Tracks primary and secondary goals
- **Command Panel** (bottom): Unit controls and building options

## Starting Configuration
- 400 Minerals, 100 Gas
- 10 Supply (4 used)
- 1 Command Center
- 1 Barracks  
- 4 SCVs

## Unit Stats

| Unit | Minerals | Gas | Supply | Health | Damage | Range | Speed |
|------|----------|-----|--------|--------|--------|-------|-------|
| SCV | 50 | 0 | 1 | 60 | 5 | 3.5 | 2.0 |
| Marine | 50 | 25 | 1 | 100 | 8 | 3.0 | 4.0 |
| Tank | 150 | 100 | 2 | 200 | 15 | 2.0 | 6.0 |

## Building Stats

| Building | Minerals | Gas | Health | Size | Special |
|----------|----------|-----|--------|------|---------|
| Command Center | 400 | 0 | 1500 | 100 | Produces SCVs, +10 supply |
| Barracks | 150 | 0 | 600 | 50 | Produces Marines/Tanks |
| Refinery | 75 | 0 | 400 | 30 | Enables gas gathering |
| Supply Depot | 100 | 0 | 300 | 25 | +8 supply |

## Building & Running

### Prerequisites
- Java 17 or higher
- Gradle 7.0 or higher

### Build
```bash
cd starcraft-lite
gradle build
```

### Run
```bash
gradle run
```

### Create Distributable JAR
```bash
gradle jar
```
The JAR will be in `build/libs/starcraft-lite-1.0.0.jar`

## Game Tips

1. **Early Game**: Send SCVs to gather minerals immediately. Right-click on blue mineral patches.
2. **Build Order**: Expand supply before you get supply blocked. Build additional barracks for faster unit production.
3. **Army Composition**: Marines are cost-effective early game. Tanks have high damage but are expensive.
4. **Map Control**: Secure the central mineral patches for additional resources.
5. **Defense**: Keep some units near your base to defend against AI attacks.

## Technical Details

- **Engine**: LWJGL3 with OpenGL rendering
- **Language**: Java 17
- **Build System**: Gradle
- **Architecture**: Entity-Component style with separate systems for rendering, input, AI, and UI

## Project Structure

```
starcraft-lite/
├── src/main/java/com/rts/
│   ├── game/           # Main game loop and window management
│   ├── entities/       # Game entities (Unit, Building, ResourceNode, etc.)
│   ├── render/         # OpenGL rendering system
│   ├── input/          # Mouse and keyboard input handling
│   ├── ui/             # User interface rendering
│   └── ai/             # AI controller with difficulty levels
├── build.gradle        # Gradle build configuration
└── README.md          # This file
```

## License

This is a fan-made educational project. StarCraft is a trademark of Blizzard Entertainment.
