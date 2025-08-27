package chef.sheesh.eyeAI.core.persistence;

import chef.sheesh.eyeAI.core.ml.models.Action;
import chef.sheesh.eyeAI.core.ml.models.GameState;

import java.io.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the persistence of machine learning models.
 * This allows the AI's learned knowledge to be saved and loaded across server restarts.
 */
public class ModelPersistence {

    private static final Logger LOGGER = Logger.getLogger(ModelPersistence.class.getName());
    private final File modelDir;

    public ModelPersistence(File modelDir) {
        this.modelDir = modelDir;
        if (!modelDir.exists()) {
            modelDir.mkdirs();
        }
    }

    /**
     * Saves the Q-table to a file.
     *
     * @param qTable The Q-table to save.
     * @param fileName The name of the file to save to.
     */
    public void saveQTable(Map<GameState, Map<Action, Double>> qTable, String fileName) {
        File file = new File(modelDir, fileName);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(qTable);
            LOGGER.info("Successfully saved Q-table to " + file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save Q-table to " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Loads a Q-table from a file.
     *
     * @param fileName The name of the file to load from.
     * @return The loaded Q-table, or null if loading fails.
     */
    @SuppressWarnings("unchecked")
    public Map<GameState, Map<Action, Double>> loadQTable(String fileName) {
        File file = new File(modelDir, fileName);
        if (!file.exists()) {
            LOGGER.info("Q-table file not found, starting with a new one: " + file.getAbsolutePath());
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                LOGGER.info("Successfully loaded Q-table from " + file.getAbsolutePath());
                return (Map<GameState, Map<Action, Double>>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load Q-table from " + file.getAbsolutePath(), e);
        }
        return null;
    }
}
