package chef.sheesh.eyeAI.core.monitoring;

import chef.sheesh.eyeAI.core.ml.models.Action;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitors the performance of AI agents in real-time.
 * Tracks metrics like action success rates, rewards, and training times.
 */
public class AIMonitor {

    // Action tracking
    private final Map<Action, AtomicLong> actionSuccessCount = new ConcurrentHashMap<>();
    private final Map<Action, AtomicLong> actionFailureCount = new ConcurrentHashMap<>();

    // Reward tracking
    private final AtomicLong totalReward = new AtomicLong(0);
    private final AtomicLong rewardSamples = new AtomicLong(0);

    // Training tracking
    private final AtomicLong totalTrainingTime = new AtomicLong(0);
    private final AtomicLong trainingBatches = new AtomicLong(0);

    // Behavior tracking
    private final Map<String, AtomicLong> behaviorDistribution = new ConcurrentHashMap<>();

    public AIMonitor() {
        for (Action action : Action.values()) {
            actionSuccessCount.put(action, new AtomicLong(0));
            actionFailureCount.put(action, new AtomicLong(0));
        }
    }

    public void recordActionSuccess(Action action) {
        actionSuccessCount.get(action).incrementAndGet();
    }

    public void recordActionFailure(Action action) {
        actionFailureCount.get(action).incrementAndGet();
    }

    public double getSuccessRatio(Action action) {
        long successes = actionSuccessCount.get(action).get();
        long failures = actionFailureCount.get(action).get();
        long total = successes + failures;
        return total == 0 ? 0 : (double) successes / total;
    }

    public void recordReward(double reward) {
        totalReward.addAndGet((long) reward);
        rewardSamples.incrementAndGet();
    }

    public double getAverageReward() {
        long samples = rewardSamples.get();
        return samples == 0 ? 0 : (double) totalReward.get() / samples;
    }

    public void recordTrainingBatch(long durationMs) {
        totalTrainingTime.addAndGet(durationMs);
        trainingBatches.incrementAndGet();
    }

    public double getAverageTrainingTime() {
        long batches = trainingBatches.get();
        return batches == 0 ? 0 : (double) totalTrainingTime.get() / batches;
    }

    public void recordBehavior(String behaviorName) {
        behaviorDistribution.computeIfAbsent(behaviorName, k -> new AtomicLong(0)).incrementAndGet();
    }

    public Map<String, Long> getBehaviorDistribution() {
        Map<String, Long> distribution = new ConcurrentHashMap<>();
        behaviorDistribution.forEach((key, value) -> distribution.put(key, value.get()));
        return distribution;
    }

    public void reset() {
        actionSuccessCount.values().forEach(v -> v.set(0));
        actionFailureCount.values().forEach(v -> v.set(0));
        totalReward.set(0);
        rewardSamples.set(0);
        totalTrainingTime.set(0);
        trainingBatches.set(0);
        behaviorDistribution.clear();
    }
}
