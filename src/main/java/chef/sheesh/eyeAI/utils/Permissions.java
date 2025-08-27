package chef.sheesh.eyeAI.utils;

import lombok.experimental.UtilityClass;

/**
 * Central permissions constants for the plugin
 */

public class Permissions {
    
    // Base permissions
    public static final String BASE = "eyeai";
    public static final String ADMIN = "eyeai.admin";
    
    // Command permissions
    public static final String COMMAND_USE = "eyeai.command.use";
    public static final String COMMAND_ADMIN = "eyeai.command.admin";
    public static final String COMMAND_STATS = "eyeai.command.stats";
    public static final String COMMAND_STATS_OTHER = "eyeai.command.stats.other";
    public static final String COMMAND_SCOREBOARD = "eyeai.command.scoreboard";
    public static final String COMMAND_SCOREBOARD_ADMIN = "eyeai.command.scoreboard.admin";
    public static final String COMMAND_SCOREBOARD_OTHER = "eyeai.command.scoreboard.other";
    
    // GUI permissions
    public static final String GUI_PLAYER = "eyeai.gui.player";
    public static final String GUI_ADMIN = "eyeai.gui.admin";
    public static final String GUI_EDIT_PLAYER = "eyeai.gui.editplayer";
    
    // Feature permissions
    public static final String EDIT_PLAYER_STATS = "eyeai.edit.player.stats";
    public static final String EDIT_PLAYER_ECONOMY = "eyeai.edit.player.economy";
    public static final String EDIT_PLAYER_PERKS = "eyeai.edit.player.perks";
    public static final String VIEW_METRICS = "eyeai.view.metrics";
    public static final String MANAGE_CONFIG = "eyeai.manage.config";
    public static final String WORLD_MANAGEMENT = "eyeai.world.manage";
}
