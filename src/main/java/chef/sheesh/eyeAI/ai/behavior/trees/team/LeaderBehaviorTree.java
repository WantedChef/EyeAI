package chef.sheesh.eyeAI.ai.behavior.trees.team;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.behavior.nodes.SequenceNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.team.HasRoleConditionNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.team.IsInTeamConditionNode;
import chef.sheesh.eyeAI.ai.core.team.TeamRole;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

public class LeaderBehaviorTree extends BehaviorTree {

    private final BehaviorTree root;

    public LeaderBehaviorTree(IFakePlayer agent) {
        this.name = "LeaderBehavior";
        this.description = "Behavior for agents with the LEADER role.";
        this.root = new SequenceNode(
            new IsInTeamConditionNode(),
            new HasRoleConditionNode(TeamRole.LEADER)
            // TODO: Add leader behaviors, like finding targets and assigning them to team members.
        );
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        // For now, the leader just stands still.
        return root.execute(fakePlayer);
    }

    @Override
    public void reset() {
        root.reset();
    }

    @Override
    public String getCategory() {
        return "Team";
    }
}
