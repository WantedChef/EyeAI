package chef.sheesh.eyeAI.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Utility class for handling color codes and formatting
 */
public class ColorUtils {
    
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    
    /**
     * Convert legacy color codes to Adventure Component
     * @param text The text with legacy color codes
     * @return The formatted Component
     */
    public static Component parse(String text) {
        // Replace legacy color codes with MiniMessage format
        String converted = text.replace("§", "&");
        return miniMessage.deserialize(converted);
    }
    
    /**
     * Parse color codes in a string
     * @param text The text with color codes
     * @return The formatted string
     */
    public static String parseToString(String text) {
        return miniMessage.serialize(parse(text));
    }
}
