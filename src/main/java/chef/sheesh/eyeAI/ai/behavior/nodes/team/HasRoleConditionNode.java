package chef.sheesh.eyeAI.ai.behavior.nodes.team;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.core.team.TeamRole;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

public class HasRoleConditionNode extends BehaviorTree {

    private final TeamRole expectedRole;

    public HasRoleConditionNode(TeamRole expectedRole) {
        this.expectedRole = expectedRole;
        this.name = "HasRoleCondition";
        this.description = "Checks if the agent has the role: " + expectedRole;
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        return fakePlayer.getRole() == expectedRole ? success() : failure();
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
