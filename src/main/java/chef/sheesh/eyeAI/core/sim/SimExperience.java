package chef.sheesh.eyeAI.core.sim;

public final class SimExperience {
    // state, action, reward, nextState, terminal
    private final long stateHash;
    private final int action;
    private final double reward;
    private final long nextStateHash;
    private final boolean terminal;
    private final double[] sequence;
    private double tdError = 0.0; // TD error for loss calculation

    public SimExperience(long stateHash, int action, double reward, long nextStateHash, boolean terminal) {
        this.stateHash = stateHash;
        this.action = action;
        this.reward = reward;
        this.nextStateHash = nextStateHash;
        this.terminal = terminal;
        this.sequence = null;
    }

    public SimExperience(double[] sequence) {
        this.stateHash = 0;
        this.action = 0;
        this.reward = 0;
        this.nextStateHash = 0;
        this.terminal = false;
        this.sequence = sequence;
    }

    // Default constructor for basic experiences
    public SimExperience() {
        this(0L, 0, 0.0, 0L, false);
    }

    // Q-learning support
    public boolean isQUpdate() {
        return sequence == null;
    }

    public long getStateHash() { return stateHash; }
    public int getAction() { return action; }
    public double getReward() { return reward; }
    public long getNextStateHash() { return nextStateHash; }
    public boolean isTerminal() { return terminal; }

    // RNN sequence support
    public boolean hasSequence() {
        return sequence != null;
    }

    public double[] getSequence() { return sequence; }

    // TD error for loss calculation
    public double getTDError() { return tdError; }
    public void setTDError(double tdError) { this.tdError = tdError; }
}
