package chef.sheesh.eyeAI.core.export;

import chef.sheesh.eyeAI.core.ml.models.Experience;
import chef.sheesh.eyeAI.core.ml.models.GameState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the export of AI training data to various formats for analysis.
 */
public class DataExporter {

    private static final Logger LOGGER = Logger.getLogger(DataExporter.class.getName());
    private final ObjectMapper jsonMapper;

    public DataExporter() {
        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Exports a list of experiences to a CSV file.
     *
     * @param experiences The list of experiences to export.
     * @param file The file to write to.
     */
    public void exportExperiencesToCsv(List<Experience> experiences, File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.append("state_x,state_y,state_z,state_health,state_hunger,action,reward,next_state_x,next_state_y,next_state_z,next_state_health,next_state_hunger\n");

            // Write data
            for (Experience exp : experiences) {
                GameState state = exp.state();
                GameState nextState = exp.nextState();

                writer.append(String.format("%f,%f,%f,%f,%d,", state.getX(), state.getY(), state.getZ(), state.getHealth(), state.getHunger()));
                writer.append(String.format("%s,%f,", exp.action().name(), exp.reward()));
                writer.append(String.format("%f,%f,%f,%f,%d\n", nextState.getX(), nextState.getY(), nextState.getZ(), nextState.getHealth(), nextState.getHunger()));
            }

            LOGGER.info("Successfully exported " + experiences.size() + " experiences to " + file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to export experiences to CSV", e);
        }
    }

    /**
     * Exports a list of experiences to a JSON file.
     *
     * @param experiences The list of experiences to export.
     * @param file The file to write to.
     */
    public void exportExperiencesToJson(List<Experience> experiences, File file) {
        try {
            jsonMapper.writeValue(file, experiences);
            LOGGER.info("Successfully exported " + experiences.size() + " experiences to " + file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to export experiences to JSON", e);
        }
    }

    /**
     * Exports a list of experiences to a binary file using Java serialization.
     *
     * @param experiences The list of experiences to export.
     * @param file The file to write to.
     */
    public void exportExperiencesToBinary(List<Experience> experiences, File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(experiences);
            LOGGER.info("Successfully exported " + experiences.size() + " experiences to " + file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to export experiences to binary file", e);
        }
    }
}
