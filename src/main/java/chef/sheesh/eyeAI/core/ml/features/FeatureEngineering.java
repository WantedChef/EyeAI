package chef.sheesh.eyeAI.core.ml.features;

import chef.sheesh.eyeAI.core.ml.models.IState;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * FeatureEngineering provides advanced feature extraction and preprocessing utilities
 * for the EyeAI ML system. Handles normalization, encoding, and feature transformation.
 */
public class FeatureEngineering {
    private static final double NORMALIZATION_EPSILON = 1e-8;

    /**
     * Normalize a feature vector to have zero mean and unit variance.
     * @param features The feature vector to normalize
     * @return Normalized feature vector
     */
    public static double[] normalizeZScore(double[] features) {
        double[] normalized = new double[features.length];
        double mean = calculateMean(features);
        double std = calculateStdDev(features, mean);

        for (int i = 0; i < features.length; i++) {
            normalized[i] = (features[i] - mean) / (std + NORMALIZATION_EPSILON);
        }

        return normalized;
    }

    /**
     * Min-max normalization to scale features to [0, 1] range.
     * @param features The feature vector to normalize
     * @param minValue Minimum value in the dataset (for consistent scaling)
     * @param maxValue Maximum value in the dataset (for consistent scaling)
     * @return Normalized feature vector in [0, 1] range
     */
    public static double[] normalizeMinMax(double[] features, double minValue, double maxValue) {
        if (Math.abs(maxValue - minValue) < NORMALIZATION_EPSILON) {
            return new double[features.length]; // Return zeros if no variance
        }

        double[] normalized = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            normalized[i] = (features[i] - minValue) / (maxValue - minValue);
        }

        return normalized;
    }

    /**
     * Convert categorical features to one-hot encoding.
     * @param categoryIndex The index of the category (0-based)
     * @param numCategories Total number of categories
     * @return One-hot encoded array
     */
    public static double[] oneHotEncode(int categoryIndex, int numCategories) {
        if (categoryIndex < 0 || categoryIndex >= numCategories) {
            throw new IllegalArgumentException("Category index out of range");
        }

        double[] encoded = new double[numCategories];
        encoded[categoryIndex] = 1.0;
        return encoded;
    }

    /**
     * Convert direction vector to angular features (sin, cos of angles).
     * @param direction The direction vector
     * @return Array containing [sin(yaw), cos(yaw), sin(pitch), cos(pitch)]
     */
    public static double[] encodeDirection(Vector direction) {
        double[] features = new double[4];

        // Calculate yaw and pitch from direction vector
        double yaw = Math.atan2(direction.getZ(), direction.getX());
        double pitch = Math.asin(direction.getY() / direction.length());

        features[0] = Math.sin(yaw);
        features[1] = Math.cos(yaw);
        features[2] = Math.sin(pitch);
        features[3] = Math.cos(pitch);

        return features;
    }

    /**
     * Encode distance to entities as radial basis function (RBF) features.
     * @param distances List of distances to nearby entities
     * @param centers RBF centers (distance thresholds)
     * @param sigma RBF width parameter
     * @return RBF encoded features
     */
    public static double[] encodeDistancesRBF(List<Double> distances, double[] centers, double sigma) {
        double[] features = new double[centers.length * distances.size()];

        int featureIndex = 0;
        for (double distance : distances) {
            for (double center : centers) {
                double diff = distance - center;
                features[featureIndex++] = Math.exp(-(diff * diff) / (2 * sigma * sigma));
            }
        }

        return features;
    }

    /**
     * Extract spatial features from a location relative to a reference point.
     * @param location The location to extract features from
     * @param referencePoint The reference location
     * @return Feature array [distance, relative_x, relative_y, relative_z, direction_sin, direction_cos]
     */
    public static double[] extractSpatialFeatures(Location location, Location referencePoint) {
        Vector relativeVector = location.toVector().subtract(referencePoint.toVector());
        double distance = relativeVector.length();

        double[] features = new double[8];
        features[0] = distance;
        features[1] = relativeVector.getX();
        features[2] = relativeVector.getY();
        features[3] = relativeVector.getZ();

        // Direction encoding
        double[] directionFeatures = encodeDirection(relativeVector);
        System.arraycopy(directionFeatures, 0, features, 4, directionFeatures.length);

        return features;
    }

    /**
     * Extract entity-related features.
     * @param entities List of nearby entities
     * @param playerLocation Player's current location
     * @return Feature array with entity positions and types
     */
    public static double[] extractEntityFeatures(List<Entity> entities, Location playerLocation) {
        if (entities.isEmpty()) {
            return new double[20]; // Return zero vector for no entities
        }

        // Limit to top 5 closest entities
        entities = entities.stream()
                .sorted((e1, e2) -> Double.compare(
                    e1.getLocation().distance(playerLocation),
                    e2.getLocation().distance(playerLocation)))
                .limit(5)
                .collect(Collectors.toList());

        double[] features = new double[entities.size() * 4]; // 4 features per entity

        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            Location entityLoc = entity.getLocation();

            // Distance and relative position
            double distance = entityLoc.distance(playerLocation);
            Vector relativeVector = entityLoc.toVector().subtract(playerLocation.toVector());

            features[i * 4] = distance;
            features[i * 4 + 1] = relativeVector.getX();
            features[i * 4 + 2] = relativeVector.getY();
            features[i * 4 + 3] = relativeVector.getZ();
        }

        return features;
    }

    /**
     * Apply feature scaling using robust statistics (median and MAD).
     * @param features The feature vector to scale
     * @param median Pre-computed median of the dataset
     * @param mad Median absolute deviation of the dataset
     * @return Robustly scaled features
     */
    public static double[] robustScale(double[] features, double median, double mad) {
        double[] scaled = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            scaled[i] = (features[i] - median) / (mad + NORMALIZATION_EPSILON);
        }
        return scaled;
    }

    /**
     * Calculate statistical features from a feature vector.
     * @param features The input feature vector
     * @return Array containing [mean, std, min, max, skewness, kurtosis]
     */
    public static double[] calculateStatisticalFeatures(double[] features) {
        double mean = calculateMean(features);
        double std = calculateStdDev(features, mean);
        double min = findMin(features);
        double max = findMax(features);
        double skewness = calculateSkewness(features, mean, std);
        double kurtosis = calculateKurtosis(features, mean, std);

        return new double[]{mean, std, min, max, skewness, kurtosis};
    }

    /**
     * Convert feature vector to INDArray for DL4J compatibility.
     * @param features The feature vector
     * @param batchSize Batch size (usually 1 for single sample)
     * @return INDArray representation
     */
    public static INDArray toINDArray(double[] features, int batchSize) {
        return Nd4j.create(features).reshape(batchSize, features.length);
    }

    // Private helper methods

    private static double calculateMean(double[] values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    private static double calculateStdDev(double[] values, double mean) {
        double sumSquaredDiffs = 0.0;
        for (double value : values) {
            double diff = value - mean;
            sumSquaredDiffs += diff * diff;
        }
        return Math.sqrt(sumSquaredDiffs / values.length);
    }

    private static double findMin(double[] values) {
        double min = Double.MAX_VALUE;
        for (double value : values) {
            if (value < min) { min = value; }
        }
        return min;
    }

    private static double findMax(double[] values) {
        double max = Double.MIN_VALUE;
        for (double value : values) {
            if (value > max) { max = value; }
        }
        return max;
    }

    private static double calculateSkewness(double[] values, double mean, double std) {
        double skewness = 0.0;
        for (double value : values) {
            double diff = value - mean;
            skewness += Math.pow(diff, 3);
        }
        skewness /= values.length;
        return skewness / Math.pow(std + NORMALIZATION_EPSILON, 3);
    }

    private static double calculateKurtosis(double[] values, double mean, double std) {
        double kurtosis = 0.0;
        for (double value : values) {
            double diff = value - mean;
            kurtosis += Math.pow(diff, 4);
        }
        kurtosis /= values.length;
        kurtosis /= Math.pow(std + NORMALIZATION_EPSILON, 4);
        return kurtosis - 3.0; // Excess kurtosis
    }
}
