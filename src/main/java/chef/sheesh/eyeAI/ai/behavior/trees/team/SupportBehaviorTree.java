package chef.sheesh.eyeAI.ai.behavior.trees.team;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.behavior.nodes.SequenceNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.team.FollowTeamMemberNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.team.HasRoleConditionNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.team.IsInTeamConditionNode;
import chef.sheesh.eyeAI.ai.core.team.TeamRole;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

public class SupportBehaviorTree extends BehaviorTree {

    private final BehaviorTree root;

    public SupportBehaviorTree(IFakePlayer agent) {
        this.name = "SupportBehavior";
        this.description = "Behavior for agents with the SUPPORT role.";
        this.root = new SequenceNode(
            new IsInTeamConditionNode(),
            new HasRoleConditionNode(TeamRole.SUPPORT),
            new FollowTeamMemberNode(TeamRole.LEADER)
            // TODO: Add more support behaviors, like healing or assisting in combat
        );
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
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
