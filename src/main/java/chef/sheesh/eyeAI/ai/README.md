# EyeAI - Complete AI Systeem volgens Guide 2

Dit is een volledig geïmplementeerd AI systeem voor Minecraft met server-side fake players, gebaseerd op Guide 2 specificaties.

## 🎯 **Kern Features**

### ✅ **Server-side Fake Players**
- Geen Citizens dependency - volledig server-side simulatie
- Packet-based visuals (optioneel via ProtocolLib)
- Thread-safe implementatie met main thread safety

### ✅ **Behavior Trees**
- Complete Behavior Tree systeem met:
  - Sequence, Selector, Condition nodes
  - Action nodes (MoveTo, Attack)
  - Factory voor standaard trees

### ✅ **Advanced Movement**
- Async A* pathfinding
- NavGraph met walkable detection
- MovementEngine voor pad optimalisatie

### ✅ **Machine Learning**
- Genetic Algorithm voor AI evolutie
- Q-learning voor online learning
- Replay Buffer voor experience replay

### ✅ **GUI Systeem**
- Framework-agnostisch GUI adapter
- Admin panel voor AI beheer
- Inventory-based interface

### ✅ **Commands**
- Uitgebreide command suite: `/ai spawn`, `/ai despawn`, `/ai list`, `/ai gui`, `/ai stats`
- Tab completion en permission checks

### ✅ **Persistence**
- Configuratie systeem met bestaande ConfigurationManager
- Persistence voor fake players, learning data en statistics

## 📁 **Package Structuur**

```
chef.sheesh.eyeAI.ai
├── core/                    # Centrale AI engine
│   ├── AIEngine.java       # Centrale hub voor het hele systeem
│   ├── SchedulerService.java # Thread-safe taak planning
│   ├── DecisionContext.java # AI beslissingscontext
│   ├── AITickEvent.java    # Synchronisatie tussen AI updates
│   └── AITickListener.java # Event handler
├── fakeplayer/             # Fake player simulatie
│   ├── FakePlayer.java     # Core simulatie object
│   ├── FakePlayerManager.java # Beheer van alle fake players
│   ├── PacketNpcController.java # ProtocolLib integratie voor visuals
│   ├── FakePlayerState.java # Enum voor states
│   └── FakePlayerWrapper.java # CommandSender interface
├── movement/               # Pathfinding & movement
│   ├── MovementEngine.java # Async pathfinding engine
│   ├── NavGraph.java       # A* algoritme met walkable detection
│   └── Path.java          # Pad optimalisatie en smoothing
├── behavior/               # Behavior tree systeem
│   ├── BehaviorTree.java   # Abstract base class
│   ├── BehaviorTreeFactory.java # Factory voor standaard trees
│   └── nodes/              # Verschillende node types
│       ├── CompositeNode.java
│       ├── SequenceNode.java
│       ├── SelectorNode.java
│       ├── ConditionNode.java
│       ├── MoveToNode.java
│       ├── AttackNode.java
│       ├── HasTargetCondition.java
│       └── HealthLowCondition.java
├── learning/               # Machine learning
│   ├── GeneticOptimizer.java # Evolutionaire algoritmen
│   ├── SimpleLearner.java    # Q-learning implementatie
│   └── ReplayBuffer.java     # Experience replay
├── commands/               # Command handlers
│   └── AICommands.java      # Uitgebreide command suite
├── gui/                    # Admin interface (framework-agnostisch)
│   ├── IFGuiAdapter.java   # Interface voor inventory frameworks
│   ├── AdminAIGui.java     # Admin panel implementatie
│   ├── AdminGuiContext.java # GUI state management
│   └── GuiItem.java        # GUI item wrapper
└── infra/                  # Persistence en configuratie
    └── PersistenceService.java # Opslaan/laden van AI data
```

## 🚀 **Snel Start**

### 1. **Initialisatie**

```java
// In je main plugin class
private AIEngine aiEngine;

@Override
public void onEnable() {
    // Initialiseer configuration
    ConfigurationManager configManager = new ConfigurationManager(this);

    // Maak AI Engine
    aiEngine = new AIEngine(this, configManager);

    // Enable het systeem
    aiEngine.enable();

    // Registreer commands
    getCommand("ai").setExecutor(new AICommands(aiEngine));
}
```

### 2. **Basic Fake Player Spawn**

```java
// Spawn een enkele fake player
Location spawnLocation = player.getLocation();
aiEngine.getFakePlayerManager().spawnFakePlayer("AI_Guard", spawnLocation);

// Of via command: /ai spawn 1 Guard
```

### 3. **Custom Behavior Tree**

```java
// Maak custom behavior tree
BehaviorTree tree = new SelectorNode(
    new SequenceNode(
        new HasTargetCondition(),
        new AttackNode(targetEntity)
    ),
    new MoveToNode(patrolLocation)
);

// Attach aan fake player
fakePlayer.setBehaviorTree(tree);
```

## ⚙️ **Configuratie**

Het systeem gebruikt de bestaande `ConfigurationManager`. Belangrijke instellingen:

```yaml
ai:
  enabled: true
  max-fakeplayers: 100
  learning:
    enabled: true
    population-size: 50
  visual:
    enabled: false  # ProtocolLib vereist voor visuals
```

## 📊 **Commands**

- `/ai spawn <amount> [name]` - Spawn fake players
- `/ai despawn [name]` - Despawn fake players
- `/ai list` - List active fake players
- `/ai gui` - Open admin GUI
- `/ai stats` - Systeem statistieken
- `/ai kill <name>` - Kill a fake player
- `/ai teleport <name>` - Teleport to a fake player
- `/ai debug <on|off|info>` - Debug commands

## 🌳 **Behavior Trees**

### **Voorbeeld Combat Tree:**

```java
// Factory maakt standaard combat tree
BehaviorTree combatTree = new BehaviorTreeFactory().createDefaultCombatTree();

// Of bouw custom tree
BehaviorTree fleeTree = new SequenceNode(
    new HealthLowCondition(),
    new MoveToNode(safeLocation)
);

BehaviorTree mainTree = new SelectorNode(
    fleeTree,           // Flee if health low
    combatSequence,     // Fight if enemy nearby
    patrolAction        // Otherwise patrol
);
```

### **Beschikbare Nodes:**

**Composite Nodes:**
- `SequenceNode` - Voer kinderen uit in volgorde
- `SelectorNode` - Voer kinderen uit tot één slaagt

**Action Nodes:**
- `MoveToNode` - Beweeg naar locatie
- `AttackNode` - Val doelwit aan

**Condition Nodes:**
- `HasTargetCondition` - Controleer of er een doelwit is
- `HealthLowCondition` - Controleer of gezondheid laag is

## 🧠 **Machine Learning**

### **Genetic Algorithm:**
```java
GeneticOptimizer optimizer = new GeneticOptimizer(scheduler);
// Evolution gebeurt automatisch elke generatie
```

### **Q-Learning:**
```java
SimpleLearner learner = new SimpleLearner(fakePlayer);
// Learning gebeurt tijdens gameplay
```

## 🔄 **Lifecycle Management**

Het systeem volgt Bukkit's plugin lifecycle:

- **Enable**: Start scheduler, laad persistence, registreer events
- **Disable**: Despawn alle fake players, stop scheduler
- **Tick**: Elke tick worden alle fake players geüpdatet

## 🚦 **Thread Safety**

- **Main Thread**: Alle Bukkit API calls en events
- **Async Thread**: Pathfinding, GA, learning calculations
- **SchedulerService**: Beheert thread veiligheid

## 📈 **Performance**

- **Adaptive Throttling**: Vermindert updates bij lage TPS
- **Packet Batching**: Groepeert visual updates
- **Path Caching**: Hergebruikt berekende paden
- **Memory Management**: Cleanup van inactieve entities

## 🐛 **Debugging**

Enable debug mode voor gedetailleerde logging:
- `/ai debug on` - Enable debug output
- `/ai debug info` - Toon systeem informatie
- Config: `ai.debug.enabled: true`

## 🎮 **Gebruik Cases**

### **Combat AI**
```java
FakePlayer guard = aiEngine.getFakePlayerManager().spawnFakePlayer("Guard", location);
guard.setBehaviorTree(BehaviorTreeFactory.createCombatTree());
```

### **Patrol AI**
```java
FakePlayer patroller = aiEngine.getFakePlayerManager().spawnFakePlayer("Patrol", location);
patroller.setBehaviorTree(BehaviorTreeFactory.createPatrolTree());
```

### **Learning AI**
```java
FakePlayer learner = aiEngine.getFakePlayerManager().spawnFakePlayer("LearningAI", location);
// Het systeem leert automatisch tijdens interacties
```

## 🔧 **Uitbreiding**

Het systeem is ontworpen om uitbreidbaar te zijn:

- **Nieuwe Behavior Nodes**: Extend `BehaviorTree` en `CompositeNode`
- **Custom Learning**: Implementeer eigen learning algoritmes
- **Nieuwe Commands**: Extend `AICommands`
- **GUI Frameworks**: Implementeer `IFGuiAdapter`

## 📋 **Checklist voor Deployment**

- [ ] ProtocolLib geïnstalleerd (voor visuals)
- [ ] Configuratie gecontroleerd
- [ ] Permissions ingesteld
- [ ] Test in development omgeving
- [ ] Monitor TPS tijdens gebruik
- [ ] Backup van persistence data

---

**Status**: ✅ Volledig geïmplementeerd volgens Guide 2
**Klaar voor**: Productie gebruik
**Compatibiliteit**: Minecraft 1.16+ (Bukkit/Spigot/Paper)

**Hoofdcomponenten geïntegreerd in bestaande EyeAI structuur!** 🎉

