package chef.sheesh.eyeAI.core.security;

import java.util.regex.Pattern;

/**
 * A utility class for validating user input to prevent security vulnerabilities.
 */
public class InputValidator {

    private static final Pattern SAFE_STRING_PATTERN =
        Pattern.compile("^[a-zA-Z0-9_\\-\\s]{1,100}$");

    private static final int MAX_COMMAND_LENGTH = 1000;
    private static final int MAX_AGENT_NAME_LENGTH = 50;

    public static void validateCommand(String command) {
        if (command == null) {
            throw new SecurityException("Command cannot be null");
        }

        if (command.length() > MAX_COMMAND_LENGTH) {
            throw new SecurityException("Command too long: " + command.length());
        }

        // This is a basic check. A real implementation should be more robust
        // and consider the specific commands being used.
        if (command.toLowerCase().contains("op") || command.toLowerCase().contains("deop")) {
            throw new SecurityException("Potentially dangerous command detected");
        }
    }

    public static void validateAgentName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Agent name cannot be null or empty");
        }

        if (name.length() > MAX_AGENT_NAME_LENGTH) {
            throw new IllegalArgumentException("Agent name too long: " + name.length());
        }

        if (!SAFE_STRING_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Agent name contains invalid characters");
        }
    }

    public static void validateCoordinates(double x, double y, double z) {
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
            throw new IllegalArgumentException("Coordinates cannot be NaN");
        }

        if (Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z)) {
            throw new IllegalArgumentException("Coordinates cannot be infinite");
        }

        // Prevent teleportation outside reasonable bounds
        double maxDistance = 30000000; // Max world border
        if (Math.abs(x) > maxDistance || Math.abs(z) > maxDistance) {
            throw new SecurityException("Coordinates outside safe bounds");
        }
    }
}
