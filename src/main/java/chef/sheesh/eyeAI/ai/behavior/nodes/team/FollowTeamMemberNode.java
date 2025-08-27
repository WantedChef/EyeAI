package chef.sheesh.eyeAI.ai.behavior.nodes.team;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.behavior.nodes.MoveToNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.SequenceNode;
import chef.sheesh.eyeAI.ai.core.team.TeamRole;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

public class FollowTeamMemberNode extends BehaviorTree {

    private final BehaviorTree root;
    private static final String LEADER_KEY = "team_leader_to_follow";

    public FollowTeamMemberNode(TeamRole roleToFollow) {
        this.name = "FollowTeamMember";
        this.description = "Follows a team member with role " + roleToFollow;
        this.root = new SequenceNode(
            new FindTeamMemberNode(roleToFollow, LEADER_KEY),
            new MoveToNode(LEADER_KEY)
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
