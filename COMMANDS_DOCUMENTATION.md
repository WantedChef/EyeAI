# EyeAI Plugin - Command Registration Guide

This document explains how to properly register commands in the EyeAI Minecraft plugin and other important implementation details.

## Table of Contents
1. [Command Registration Process](#command-registration-process)
2. [Plugin Configuration File](#plugin-configuration-file)
3. [Command Implementation Structure](#command-implementation-structure)
4. [Creating New Commands](#creating-new-commands)
5. [Common Issues and Solutions](#common-issues-and-solutions)

## Command Registration Process

The EyeAI plugin uses a two-step command registration process:

1. **Plugin Configuration**: Commands are defined in `src/main/resources/plugin.yml`
2. **Code Registration**: Commands are registered in `ChefAI.java` during the `onEnable()` method

### Plugin Configuration (plugin.yml)

All commands must be defined in the `plugin.yml` file:

```yaml
commands:
  chefai:
    description: Open the CHEF-AI control center
    usage: /<command>
    permission: eyeai.use
  eyeadmin:
    description: Open the CHEF-AI admin panel
    usage: /<command>
    permission: eyeai.admin
  # ... other commands
```

### Code Registration (ChefAI.java)

Commands are registered in the `registerCommandsFromYml()` method in `ChefAI.java`:

```java
private void registerCommandsFromYml() {
    // Register chefai command
    if (getCommand("chefai") != null) {
        getCommand("chefai").setExecutor(new EyeAICommand(this));
        getLogger().info("Successfully registered chefai command");
    }
    
    // Register ai command
    if (getCommand("ai") != null) {
        getCommand("ai").setExecutor(new AICommands(aiEngine));
        getLogger().info("Successfully registered ai command");
    }
    // ... other command registrations
}
```

## Plugin Configuration File

The plugin uses `plugin.yml` (not `paper-plugin.yml`) for Bukkit to properly register commands. Make sure:

1. The file is named exactly `plugin.yml`
2. All commands are defined in the `commands` section
3. Each command has a proper description, usage, and permission

## Command Implementation Structure

All command classes should be placed in the `chef.sheesh.eyeAI.ai.commands` package.

### Base Command Class

Commands should extend `BaseCommand` which provides:
- Common error handling
- Permission checking
- Player-only execution (when needed)

### Example Command Implementation

```java
public class EyeAICommand extends BaseCommand {
    private final ChefAI plugin;
    
    public EyeAICommand(ChefAI plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Command implementation
        return true;
    }
}
```

### Special Case: AICommands

The `AICommands` class requires an `IAIEngine` instance instead of the plugin instance:

```java
// In ChefAI.java
getCommand("ai").setExecutor(new AICommands(aiEngine));
```

## Creating New Commands

To add a new command to the plugin:

1. **Define in plugin.yml**:
   ```yaml
   commands:
     newcommand:
       description: Description of the new command
       usage: /<command> [args]
       permission: eyeai.newcommand
   ```

2. **Create Command Class**:
   - Create a new class in `chef.sheesh.eyeAI.ai.commands` package
   - Extend `BaseCommand`
   - Implement constructor and `onCommand` method

3. **Register in ChefAI.java**:
   ```java
   if (getCommand("newcommand") != null) {
       getCommand("newcommand").setExecutor(new NewCommandClass(this));
       getLogger().info("Successfully registered newcommand");
   }
   ```

## Common Issues and Solutions

### 1. Commands Not Found

**Problem**: "Command newcommand not found in plugin.yml"

**Solution**: 
- Verify the command is defined in `plugin.yml`
- Check that the file is named `plugin.yml` (not `paper-plugin.yml`)
- Ensure the command name matches exactly between `plugin.yml` and `ChefAI.java`

### 2. Package Declaration Mismatches

**Problem**: Compilation errors about missing classes

**Solution**:
- Ensure all command classes have the correct package declaration: `package chef.sheesh.eyeAI.ai.commands;`
- Update imports in `ChefAI.java` and `CommandManager.java` to use the correct package

### 3. AICommands Constructor Issues

**Problem**: Incompatible types when creating AICommands

**Solution**:
- Pass an `IAIEngine` instance instead of the plugin instance
- Ensure `AIEngine` is properly initialized in `ChefAI.java`

### 4. Building the Plugin

To build the plugin JAR file:

1. Navigate to the project root directory
2. Run: `./gradlew.bat shadowJar` (Windows) or `./gradlew shadowJar` (Linux/Mac)
3. The built JAR will be located in `build/libs/`

### 5. Moving Command Classes

When moving command classes between packages:

1. Update the package declaration in the moved class
2. Update imports in all classes that reference the moved class
3. Verify the package structure matches the directory structure

This documentation should help you properly register and manage commands in the EyeAI plugin.
