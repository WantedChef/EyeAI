package chef.sheesh.eyeAI.core.sim;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import chef.sheesh.eyeAI.core.ml.MLCore;
import chef.sheesh.eyeAI.core.ml.MLManager;
import chef.sheesh.eyeAI.core.sim.SimExperience;
import chef.sheesh.eyeAI.infra.events.EventBus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance monitor specifically for ML training sessions with fake players.
 * Tracks training efficiency, learning progress, and system performance during training.
 */
public final class MLTrainingMonitor {

    private final FakePlayerManager fakePlayerManager;
    private final MLCore mlCore;
    private final EventBus eventBus;

    // Training session tracking
    private boolean isTrainingActive = false;
    private long trainingStartTime = 0;
    private int initialFakePlayerCount = 0;

    // Performance metrics
    private final AtomicLong totalTicks = new AtomicLong(0);
    private final AtomicLong totalTickTimeMs = new AtomicLong(0);
    private final AtomicLong totalExperiencesGenerated = new AtomicLong(0);
    private final AtomicLong totalRewardsGenerated = new AtomicLong(0);

    // Learning progress tracking
    private final ConcurrentMap<String, Double> averageRewardsPerAction = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> actionCounts = new ConcurrentHashMap<>();
    private double bestReward = Double.NEGATIVE_INFINITY;
    private double worstReward = Double.POSITIVE_INFINITY;

    // Performance thresholds
    private static final long MAX_TICK_TIME_MS = 50; // Max 50ms per tick for smooth gameplay
    private static final double MIN_LEARNING_RATE = 0.001; // Minimum reward improvement rate

    public MLTrainingMonitor(FakePlayerManager fakePlayerManager, MLCore mlCore, EventBus eventBus) {
        this.fakePlayerManager = fakePlayerManager;
        this.mlCore = mlCore;
        this.eventBus = eventBus;
    }

    /**
     * Start a new training session
     */
    public void startTrainingSession(int fakePlayerCount) {
        isTrainingActive = true;
        trainingStartTime = System.currentTimeMillis();
        initialFakePlayerCount = fakePlayerCount;

        // Reset metrics
        totalTicks.set(0);
        totalTickTimeMs.set(0);
        totalExperiencesGenerated.set(0);
        totalRewardsGenerated.set(0);
        averageRewardsPerAction.clear();
        actionCounts.clear();
        bestReward = Double.NEGATIVE_INFINITY;
        worstReward = Double.POSITIVE_INFINITY;

        eventBus.post(new TrainingSessionStartedEvent(fakePlayerCount));
    }

    /**
     * End the current training session
     */
    public void endTrainingSession() {
        if (!isTrainingActive) {
            return;
        }

        long trainingDurationMs = System.currentTimeMillis() - trainingStartTime;
        TrainingSessionSummary summary = generateSummary(trainingDurationMs);

        eventBus.post(new TrainingSessionEndedEvent(summary));
        isTrainingActive = false;
    }

    /**
     * Record a training tick
     */
    public void recordTick(long tickTimeMs, int activeFakePlayerCount) {
        if (!isTrainingActive) {
            return;
        }

        totalTicks.incrementAndGet();
        totalTickTimeMs.addAndGet(tickTimeMs);

        // Check performance thresholds
        if (tickTimeMs > MAX_TICK_TIME_MS) {
            eventBus.post(new PerformanceWarningEvent(
                "Training tick took too long: " + tickTimeMs + "ms",
                tickTimeMs,
                activeFakePlayerCount
            ));
        }

        // Periodic performance reports
        if (totalTicks.get() % 100 == 0) { // Every 100 ticks
            generatePeriodicReport();
        }
    }

    /**
     * Record an experience generated during training
     */
    public void recordExperience(FakePlayer fakePlayer, SimExperience experience, int action, double reward) {
        if (!isTrainingActive) {
            return;
        }

        totalExperiencesGenerated.incrementAndGet();
        totalRewardsGenerated.addAndGet((long) reward);

        // Update best/worst rewards
        if (reward > bestReward) {
            bestReward = reward;
        }
        if (reward < worstReward) {
            worstReward = reward;
        }

        // Track action-specific rewards
        String actionKey = "action_" + action;
        actionCounts.merge(actionKey, 1L, Long::sum);

        // Update running average for this action
        double currentAvg = averageRewardsPerAction.getOrDefault(actionKey, 0.0);
        long actionCount = actionCounts.get(actionKey);
        double newAvg = (currentAvg * (actionCount - 1) + reward) / actionCount;
        averageRewardsPerAction.put(actionKey, newAvg);
    }

    /**
     * Generate a periodic performance report
     */
    private void generatePeriodicReport() {
        long avgTickTime = totalTicks.get() > 0 ? totalTickTimeMs.get() / totalTicks.get() : 0;
        long experiencesPerSecond = totalTicks.get() > 0 ?
            (totalExperiencesGenerated.get() * 1000) / (System.currentTimeMillis() - trainingStartTime) : 0;

        TrainingPerformanceReport report = new TrainingPerformanceReport(
            totalTicks.get(),
            avgTickTime,
            totalExperiencesGenerated.get(),
            experiencesPerSecond,
            totalRewardsGenerated.get(),
            bestReward,
            worstReward,
            new ConcurrentHashMap<>(averageRewardsPerAction)
        );

        eventBus.post(new TrainingPerformanceReportEvent(report));
    }

    /**
     * Generate a summary of the training session
     */
    private TrainingSessionSummary generateSummary(long trainingDurationMs) {
        long avgTickTime = totalTicks.get() > 0 ? totalTickTimeMs.get() / totalTicks.get() : 0;
        long totalExperiences = totalExperiencesGenerated.get();
        long totalRewards = totalRewardsGenerated.get();
        double avgReward = totalExperiences > 0 ? (double) totalRewards / totalExperiences : 0.0;

        // Calculate learning efficiency
        double experiencesPerSecond = trainingDurationMs > 0 ?
            (double) totalExperiences / (trainingDurationMs / 1000.0) : 0.0;

        // Get final ML statistics
        MLManager.MLStatistics mlStats = mlCore.getStatistics();

        return new TrainingSessionSummary(
            trainingDurationMs,
            initialFakePlayerCount,
            totalTicks.get(),
            avgTickTime,
            totalExperiences,
            experiencesPerSecond,
            totalRewards,
            avgReward,
            bestReward,
            worstReward,
            mlStats,
            new ConcurrentHashMap<>(averageRewardsPerAction)
        );
    }

    /**
     * Get current training statistics
     */
    public TrainingSessionSummary getCurrentStats() {
        if (!isTrainingActive) {
            return null;
        }

        long currentDuration = System.currentTimeMillis() - trainingStartTime;
        return generateSummary(currentDuration);
    }

    // Event classes

    public static class TrainingSessionStartedEvent {
        public final int fakePlayerCount;
        public TrainingSessionStartedEvent(int fakePlayerCount) {
            this.fakePlayerCount = fakePlayerCount;
        }
    }

    public static class TrainingSessionEndedEvent {
        public final TrainingSessionSummary summary;
        public TrainingSessionEndedEvent(TrainingSessionSummary summary) {
            this.summary = summary;
        }
    }

    public static class PerformanceWarningEvent {
        public final String message;
        public final long tickTimeMs;
        public final int activeFakePlayerCount;

        public PerformanceWarningEvent(String message, long tickTimeMs, int activeFakePlayerCount) {
            this.message = message;
            this.tickTimeMs = tickTimeMs;
            this.activeFakePlayerCount = activeFakePlayerCount;
        }
    }

    public static class TrainingPerformanceReportEvent {
        public final TrainingPerformanceReport report;
        public TrainingPerformanceReportEvent(TrainingPerformanceReport report) {
            this.report = report;
        }
    }

    // Data classes

    public static class TrainingPerformanceReport {
        public final long totalTicks;
        public final long averageTickTimeMs;
        public final long totalExperiences;
        public final long experiencesPerSecond;
        public final long totalRewards;
        public final double bestReward;
        public final double worstReward;
        public final ConcurrentMap<String, Double> averageRewardsPerAction;

        public TrainingPerformanceReport(long totalTicks, long averageTickTimeMs, long totalExperiences,
                                       long experiencesPerSecond, long totalRewards, double bestReward,
                                       double worstReward, ConcurrentMap<String, Double> averageRewardsPerAction) {
            this.totalTicks = totalTicks;
            this.averageTickTimeMs = averageTickTimeMs;
            this.totalExperiences = totalExperiences;
            this.experiencesPerSecond = experiencesPerSecond;
            this.totalRewards = totalRewards;
            this.bestReward = bestReward;
            this.worstReward = worstReward;
            this.averageRewardsPerAction = averageRewardsPerAction;
        }
    }

    public static class TrainingSessionSummary {
        public final long trainingDurationMs;
        public final int initialFakePlayerCount;
        public final long totalTicks;
        public final long averageTickTimeMs;
        public final long totalExperiences;
        public final double experiencesPerSecond;
        public final long totalRewards;
        public final double averageReward;
        public final double bestReward;
        public final double worstReward;
        public final MLManager.MLStatistics finalMLStats;
        public final ConcurrentMap<String, Double> finalAverageRewardsPerAction;

        public TrainingSessionSummary(long trainingDurationMs, int initialFakePlayerCount, long totalTicks,
                                    long averageTickTimeMs, long totalExperiences, double experiencesPerSecond,
                                    long totalRewards, double averageReward, double bestReward, double worstReward,
                                    MLManager.MLStatistics finalMLStats, ConcurrentMap<String, Double> finalAverageRewardsPerAction) {
            this.trainingDurationMs = trainingDurationMs;
            this.initialFakePlayerCount = initialFakePlayerCount;
            this.totalTicks = totalTicks;
            this.averageTickTimeMs = averageTickTimeMs;
            this.totalExperiences = totalExperiences;
            this.experiencesPerSecond = experiencesPerSecond;
            this.totalRewards = totalRewards;
            this.averageReward = averageReward;
            this.bestReward = bestReward;
            this.worstReward = worstReward;
            this.finalMLStats = finalMLStats;
            this.finalAverageRewardsPerAction = finalAverageRewardsPerAction;
        }
    }
}
