package chef.sheesh.eyeAI.core.ml;

import chef.sheesh.eyeAI.infra.events.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Persistence manager for ML models with JSON, backups, cleanup, and event handling.
 */
public class MLModelPersistenceManager {

    private final File modelsFolder;
    private final Gson gson;
    private final EventBus eventBus;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public MLModelPersistenceManager(File dataFolder, EventBus eventBus) {
        this.modelsFolder = new File(dataFolder, "ml_models");
        modelsFolder.mkdirs();
        this.eventBus = eventBus;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new com.google.gson.JsonPrimitive(DATE_FORMATTER.format(src)))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), DATE_FORMATTER))
                .setPrettyPrinting()
                .create();
    }

    public void saveModels(MLManager.MLModels models) {
        String timestamp = DATE_FORMATTER.format(LocalDateTime.now());
        String filename = "ml_models_" + timestamp + ".json";
        File modelFile = new File(modelsFolder, filename);
        try (FileWriter writer = new FileWriter(modelFile)) {
            gson.toJson(models, writer);
            if (eventBus != null) eventBus.post(new ModelSavedEvent(filename, models.exportTime, true));
        } catch (Exception e) {
            if (eventBus != null) eventBus.post(new ModelSavedEvent(filename, models.exportTime, false));
            throw new RuntimeException("Failed to save models", e);
        }
    }

    public Optional<MLManager.MLModels> loadLatestModels() {
        File[] modelFiles = modelsFolder.listFiles(f -> f.getName().endsWith(".json"));
        if (modelFiles == null || modelFiles.length == 0) {
            return Optional.empty();
        }
        File latest = Arrays.stream(modelFiles).max(Comparator.comparingLong(File::lastModified)).orElse(null);
        if (latest == null) {
            return Optional.empty();
        }
        try (FileReader reader = new FileReader(latest)) {
            MLManager.MLModels models = gson.fromJson(reader, MLManager.MLModels.class);
            if (eventBus != null) eventBus.post(new ModelLoadedEvent(latest.getName(), models.exportTime, true));
            return Optional.of(models);
        } catch (Exception e) {
            if (eventBus != null) eventBus.post(new ModelLoadedEvent(latest.getName(), System.currentTimeMillis(), false));
            throw new RuntimeException("Failed to load models", e);
        }
    }

    public void saveModelsWithBackup(MLManager.MLModels models) {
        loadLatestModels().ifPresent(current -> {
            File backup = new File(modelsFolder, "backup_" + current.exportTime + ".json");
            new File(modelsFolder, "ml_models_latest.json").renameTo(backup);
        });
        saveModels(models);
    }

    public File[] listModelFiles() {
        return modelsFolder.listFiles(f -> f.getName().endsWith(".json"));
    }

    public Optional<MLManager.MLModels> loadModelFile(String filename) {
        File file = new File(modelsFolder, filename);
        if (!file.exists()) {
            return Optional.empty();
        }
        try (FileReader reader = new FileReader(file)) {
            MLManager.MLModels models = gson.fromJson(reader, MLManager.MLModels.class);
            if (eventBus != null) eventBus.post(new ModelLoadedEvent(filename, models.exportTime, true));
            return Optional.of(models);
        } catch (Exception e) {
            if (eventBus != null) eventBus.post(new ModelLoadedEvent(filename, System.currentTimeMillis(), false));
            throw new RuntimeException("Failed to load model file", e);
        }
    }

    public void cleanupOldModels(int keepCount) {
        File[] files = listModelFiles();
        if (files == null || files.length <= keepCount) {
            return;
        }
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        for (int i = keepCount; i < files.length; i++) {
            if (files[i].delete()) {
                if (eventBus != null) eventBus.post(new ModelFileDeletedEvent(files[i].getName(), true));
            } else {
                if (eventBus != null) eventBus.post(new ModelFileDeletedEvent(files[i].getName(), false));
            }
        }
    }

    // Event classes omitted for brevity, assume fully implemented as before
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

    public static class ModelFileDeletedEvent {
        public final String filename;
        public final boolean success;

        public ModelFileDeletedEvent(String filename, boolean success) {
            this.filename = filename;
            this.success = success;
        }
    }
}
