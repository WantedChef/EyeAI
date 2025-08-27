package chef.sheesh.eyeAI.core.ml;

import chef.sheesh.eyeAI.infra.events.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Persistence manager for ML models using JSON file storage.
 * Handles saving and loading of trained models for Q-learning, GA, and RNN components.
 */
public class MLModelPersistenceManager {

    private final File modelsFolder;
    private final Gson gson;
    private final EventBus eventBus;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MLModelPersistenceManager(File dataFolder, EventBus eventBus) {
        this.modelsFolder = new File(dataFolder, "ml_models");
        if (!this.modelsFolder.exists()) {
            this.modelsFolder.mkdirs();
        }
        this.eventBus = eventBus;

        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();
    }

    /**
     * Save ML models to file
     */
    public void saveModels(MLManager.MLModels models) {
        String timestamp = DATE_FORMATTER.format(LocalDateTime.now());
        String filename = "ml_models_" + timestamp.replace(" ", "_").replace(":", "-") + ".json";
        File modelFile = new File(modelsFolder, filename);

        try (FileWriter writer = new FileWriter(modelFile)) {
            gson.toJson(models, writer);
            eventBus.post(new ModelSavedEvent(modelFile.getName(), models.exportTime, true));
        } catch (IOException e) {
            eventBus.post(new ModelSavedEvent(filename, models.exportTime, false));
            throw new RuntimeException("Failed to save ML models to file", e);
        }
    }

    /**
     * Load the most recent ML models from file
     */
    public Optional<MLManager.MLModels> loadLatestModels() {
        File[] modelFiles = modelsFolder.listFiles((dir, name) -> name.startsWith("ml_models_") && name.endsWith(".json"));

        if (modelFiles == null || modelFiles.length == 0) {
            return Optional.empty();
        }

        // Find the most recent model file
        File latestFile = null;
        long latestTime = 0;

        for (File file : modelFiles) {
            long fileTime = file.lastModified();
            if (fileTime > latestTime) {
                latestTime = fileTime;
                latestFile = file;
            }
        }

        if (latestFile == null) {
            return Optional.empty();
        }

        try (FileReader reader = new FileReader(latestFile)) {
            MLManager.MLModels models = gson.fromJson(reader, MLManager.MLModels.class);
            eventBus.post(new ModelLoadedEvent(latestFile.getName(), models.exportTime, true));
            return Optional.of(models);
        } catch (Exception e) {
            eventBus.post(new ModelLoadedEvent(latestFile.getName(), System.currentTimeMillis(), false));
            throw new RuntimeException("Failed to load ML models from file", e);
        }
    }

    /**
     * Save models with automatic backup of previous version
     */
    public void saveModelsWithBackup(MLManager.MLModels models) {
        // Create backup of current latest model if it exists
        Optional<MLManager.MLModels> currentModels = loadLatestModels();
        if (currentModels.isPresent()) {
            String backupName = "backup_ml_models_" + System.currentTimeMillis() + ".json";
            File backupFile = new File(modelsFolder, backupName);

            // Move current latest to backup
            File[] files = modelsFolder.listFiles((dir, name) -> name.startsWith("ml_models_") && !name.contains("backup"));
            if (files != null && files.length > 0) {
                File currentLatest = files[0];
                if (currentLatest.renameTo(backupFile)) {
                    eventBus.post(new ModelBackupCreatedEvent(backupFile.getName(), true));
                } else {
                    eventBus.post(new ModelBackupCreatedEvent(backupName, false));
                }
            }
        }

        // Save new models
        saveModels(models);
    }

    /**
     * List all available model files
     */
    public File[] listModelFiles() {
        return modelsFolder.listFiles((dir, name) -> name.endsWith(".json"));
    }

    /**
     * Load specific model file by name
     */
    public Optional<MLManager.MLModels> loadModelFile(String filename) {
        File modelFile = new File(modelsFolder, filename);

        if (!modelFile.exists()) {
            return Optional.empty();
        }

        try (FileReader reader = new FileReader(modelFile)) {
            MLManager.MLModels models = gson.fromJson(reader, MLManager.MLModels.class);
            eventBus.post(new ModelLoadedEvent(filename, models.exportTime, true));
            return Optional.of(models);
        } catch (Exception e) {
            eventBus.post(new ModelLoadedEvent(filename, System.currentTimeMillis(), false));
            throw new RuntimeException("Failed to load ML models from file: " + filename, e);
        }
    }

    /**
     * Clean up old model files, keeping only the N most recent
     */
    public void cleanupOldModels(int keepCount) {
        File[] modelFiles = modelsFolder.listFiles((dir, name) -> name.startsWith("ml_models_") && name.endsWith(".json"));

        if (modelFiles == null || modelFiles.length <= keepCount) {
            return;
        }

        // Sort by last modified time (newest first)
        java.util.Arrays.sort(modelFiles, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        // Delete older files
        for (int i = keepCount; i < modelFiles.length; i++) {
            if (modelFiles[i].delete()) {
                eventBus.post(new ModelFileDeletedEvent(modelFiles[i].getName(), true));
            } else {
                eventBus.post(new ModelFileDeletedEvent(modelFiles[i].getName(), false));
            }
        }
    }

    // Custom adapters for complex types

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(DATE_FORMATTER.format(src));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), DATE_FORMATTER);
        }
    }

    // Event classes

    public static class ModelSavedEvent {
        public final String filename;
        public final long exportTime;
        public final boolean success;

        public ModelSavedEvent(String filename, long exportTime, boolean success) {
            this.filename = filename;
            this.exportTime = exportTime;
            this.success = success;
        }
    }

    public static class ModelLoadedEvent {
        public final String filename;
        public final long loadTime;
        public final boolean success;

        public ModelLoadedEvent(String filename, long loadTime, boolean success) {
            this.filename = filename;
            this.loadTime = loadTime;
            this.success = success;
        }
    }

    public static class ModelBackupCreatedEvent {
        public final String backupFilename;
        public final boolean success;

        public ModelBackupCreatedEvent(String backupFilename, boolean success) {
            this.backupFilename = backupFilename;
            this.success = success;
        }
    }

    public static class ModelFileDeletedEvent {
        public final String filename;
        public final boolean success;

        public ModelFileDeletedEvent(String filename, boolean success) {
            this.filename = filename;
            this.success = success;
        }
    }
}
