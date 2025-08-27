package chef.sheesh.eyeAI.ai.behavior.nodes.team;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

public class IsInTeamConditionNode extends BehaviorTree {

    public IsInTeamConditionNode() {
        this.name = "IsInTeamCondition";
        this.description = "Checks if the agent is in a team.";
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        return fakePlayer.getTeam() != null ? success() : failure();
    }

    @Override
    public void reset() {
        // No state
    }

    @Override
    public String getCategory() {
        return "Condition";
    }
}
