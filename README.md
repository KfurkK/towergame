# Tower Defense Game

A Java-based tower defense game built with JavaFX featuring strategic gameplay, multiple tower types, enemy waves, and a scoring system.

## 🎮 Game Overview

Tower Defense is a strategic game where players must defend their base from waves of enemies by strategically placing different types of towers along the enemy path. Each tower has unique abilities and costs, requiring players to manage their resources wisely.

## ✨ Features

### Core Gameplay
- **5 Progressive Levels** - Each level increases in difficulty with more enemy waves
- **Wave-based Combat** - Enemies spawn in waves with increasing intensity
- **Resource Management** - Manage money to purchase and upgrade towers
- **Lives System** - Lose lives when enemies reach your base
- **Scoring System** - Earn points for defeating enemies and surviving waves

### Tower Types
1. **Single Shot Tower** - Basic tower with moderate damage and range
2. **Laser Tower** - Continuous beam damage with high accuracy
3. **Triple Shot Tower** - Fires three bullets simultaneously
4. **Missile Launcher Tower** - Area damage with splash effects

### Enemy Types
- **Soldier** - Basic enemy with moderate health
- **Archer** - Faster enemy with ranged attacks
- **Giant** - Slow but heavily armored enemy

### Game Features
- **Visual Range Indicators** - See tower attack ranges when selected
- **Health Bars** - Monitor enemy health during combat
- **Background Music** - Immersive audio experience
- **High Score System** - Track and display top scores
- **Smooth Animations** - JavaFX-powered visual effects

## 🛠️ Technical Details

### Technology Stack
- **Java** - Core programming language
- **JavaFX** - GUI framework for graphics and animations
- **Java Sound API** - Audio playback for background music

### Project Structure
```
towergame/
├── src/                          # Main source code
│   ├── Main.java                 # Main application class
│   ├── Game.java                 # Game logic and entity management
│   ├── Tower.java                # Abstract tower base class
│   ├── SingleShotTower.java      # Single shot tower implementation
│   ├── LaserTower.java           # Laser tower implementation
│   ├── TripleShotTower.java      # Triple shot tower implementation
│   ├── MissileLauncherTower.java # Missile launcher tower implementation
│   ├── Enemy.java                # Base enemy class
│   ├── Archer.java               # Archer enemy implementation
│   ├── Giant.java                # Giant enemy implementation
│   ├── Bullet.java               # Projectile class
│   ├── Missile.java              # Missile projectile class
│   ├── ScoreManager.java         # High score management
│   ├── tools.java                # Utility functions
│   ├── AudioPlayer.java          # Audio playback management
│   ├── assets/                   # Game assets
│   │   ├── background.png        # Game background
│   │   ├── soldier.png           # Soldier enemy sprite
│   │   ├── archer.png            # Archer enemy sprite
│   │   ├── giant.png             # Giant enemy sprite
│   │   ├── base.png              # Player base sprite
│   │   ├── towers/               # Tower sprites
│   │   ├── bullets/              # Projectile sprites
│   │   └── sounds/               # Audio files
│   └── levels/                   # Level configuration files
│       ├── level1.txt            # Level 1 path and wave data
│       ├── level2.txt            # Level 2 configuration
│       ├── level3.txt            # Level 3 configuration
│       ├── level4.txt            # Level 4 configuration
│       └── level5.txt            # Level 5 configuration
├── scores.txt                    # High score storage
└── README.md 
```

## 🚀 Installation & Setup

### Prerequisites
- **Java JDK 11 or higher**
- **JavaFX SDK** (included with JDK 11+ or download separately)

### Running the Game

1. **Clone or download the project**
   ```bash
   git clone <repository-url>
   cd towergame
   ```

2. **Compile the project**
   ```bash
   javac -cp "path/to/javafx-sdk/lib/*" src/*.java
   ```

3. **Run the game**
   ```bash
   java -cp "src;path/to/javafx-sdk/lib/*" Main
   ```

### Alternative: Using an IDE
1. Open the project in your preferred Java IDE (IntelliJ IDEA, Eclipse, NetBeans)
2. Ensure JavaFX is properly configured in your IDE
3. Run `Main.java` as the main class

## 🎯 How to Play

### Game Controls
- **Mouse Click** - Select and place towers
- **Tower Selection** - Click on tower buttons to select tower type
- **Range Display** - Selected towers show their attack range
- **Tower Removal** - Click on placed towers to remove them (costs money)

### Strategy Tips
1. **Start with Single Shot Towers** - They're cost-effective for early waves
2. **Cover Path Intersections** - Place towers where enemies change direction
3. **Manage Resources** - Don't overspend early; save for stronger enemies
4. **Use Range Effectively** - Position towers to cover multiple path segments
5. **Adapt to Enemy Types** - Different towers work better against different enemies

### Scoring System
- **Enemy Defeats** - Earn points for each enemy destroyed
- **Wave Completion** - Bonus points for surviving entire waves
- **Level Completion** - Large bonus for completing levels
- **Efficiency Bonus** - Higher scores for using fewer resources

## 📊 Game Mechanics

### Tower Stats
| Tower Type | Cost | Range | Damage | Special |
|------------|------|-------|--------|---------|
| Single Shot | $50 | Medium | Medium | None |
| Laser | $100 | Long | High | Continuous |
| Triple Shot | $75 | Medium | Medium | Multi-target |
| Missile | $150 | Long | High | Area damage |

### Enemy Stats
| Enemy Type | Health | Speed | Damage | Special |
|------------|--------|-------|--------|---------|
| Soldier | Low | Medium | Low | None |
| Archer | Medium | High | Medium | Ranged attack |
| Giant | High | Low | High | Armored |

### Wave Progression
- **Level 1**: 3 waves (5, 8, 12 enemies)
- **Level 2**: 4 waves (5, 8, 12, 20 enemies)
- **Level 3**: 5 waves (5, 8, 12, 20, 25 enemies)
- **Level 4**: 5 waves (increased difficulty)
- **Level 5**: 5 waves (maximum difficulty)

## 🎵 Audio Features

The game includes background music that enhances the gaming experience:
- **Background Music** - Looping soundtrack during gameplay
- **Sound Effects** - Tower firing and enemy death sounds
- **Audio Management** - Automatic audio playback and volume control

## 🏆 High Score System

The game tracks high scores with the following information:
- **Player Name** - Customizable player identifier
- **Score** - Total points earned
- **Timestamp** - Date and time of achievement
- **Persistent Storage** - Scores saved between game sessions

## 🔧 Development

### Key Classes
- **`Main`** - Application entry point, UI management, game loop
- **`Game`** - Central game logic, entity management, updates
- **`Tower`** - Abstract base class for all tower types
- **`Enemy`** - Base class for enemy entities with pathfinding
- **`ScoreManager`** - High score persistence and retrieval

### Level System
Levels are defined in text files with:
- **Grid dimensions** (WIDTH, HEIGHT)
- **Path coordinates** - Enemy movement path
- **Wave data** - Enemy count, spawn timing, delays
- API scrapes the data from relevant files.
### Asset Management
- **Sprites** - PNG images for all game entities
- **Audio** - WAV/MP3 files for music and effects
- **Levels** - Text-based configuration files

## 📝 License

This project is provided as-is for educational and entertainment purposes.

## 🤝 Contributing

Feel free to submit issues, feature requests, or pull requests to improve the game.

---

**Enjoy defending your tower!** 🏰⚔️

Note: This project is built for Marmara University CSE term project.
