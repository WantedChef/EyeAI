# Chef-AI

A production-grade AI plugin for Paper 1.20+ servers.

## Features

- **Machine Learning Core**: Deep learning models for player behavior prediction
- **Simulation Engine**: Fake player system for training AI models
- **Anti-Cheat Integration**: Detect anomalies in player behavior
- **Chat NLP**: Natural language processing for chat moderation
- **NPC Villages**: AI-powered village generation and management
- **Admin Dashboard**: In-game GUI for monitoring and configuration

## Installation

1. Build the plugin using Gradle:
   ```
   ./gradlew shadowJar
   ```
   
   Or use the provided build script:
   ```
   ./build-plugin.bat
   ```

2. Copy the generated `ChefAI.jar` to your server's `plugins` folder

3. Start/restart your server

## Dependencies

- Paper 1.20+
- ProtocolLib
- packetevents

## Configuration

The plugin can be configured through `config/chefai.yml`:

```yaml
storage:
  provider: H2 # H2 | POSTGRES
  h2:
    file: plugins/ChefAI/data/chefai
  postgres:
    host: 127.0.0.1
    port: 5432
    database: chefai
    user: postgres
    password: secret

training:
  enabled: true
  fakePlayers: 50
  batchSize: 128
  epsilon:
    start: 0.4
    min: 0.02
    adaptive: true
    decay: 0.995
  safety:
    minTPS: 18.0

ui:
  enableDashboard: true
```

## Commands

- `/chefai` - Opens the admin control center

For detailed information about command registration and implementation, see [COMMANDS_DOCUMENTATION.md](COMMANDS_DOCUMENTATION.md)

## Permissions

- `chefai.admin` - Access to admin features
