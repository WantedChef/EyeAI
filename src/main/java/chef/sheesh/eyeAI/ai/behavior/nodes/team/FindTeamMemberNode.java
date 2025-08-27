package chef.sheesh.eyeAI.ai.behavior.nodes.team;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.core.team.Team;
import chef.sheesh.eyeAI.ai.core.team.TeamRole;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

import java.util.Map;
import java.util.Optional;

public class FindTeamMemberNode extends BehaviorTree {

    private final TeamRole roleToFind;
    private final String blackboardKey;

    /**
     * Finds a team member with a specific role and stores it in the blackboard.
     * @param roleToFind The role of the team member to find.
     * @param blackboardKey The blackboard key to store the found member under.
     */
    public FindTeamMemberNode(TeamRole roleToFind, String blackboardKey) {
        this.roleToFind = roleToFind;
        this.blackboardKey = blackboardKey;
        this.name = "FindTeamMember";
        this.description = "Finds a team member with role " + roleToFind;
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        Team team = fakePlayer.getTeam();
        if (team == null) {
            return failure();
        }

        Optional<IFakePlayer> foundMember = team.getMembers().entrySet().stream()
                .filter(entry -> entry.getValue() == roleToFind)
                .map(Map.Entry::getKey)
                .findFirst();

        if (foundMember.isPresent()) {
            fakePlayer.getBlackboard().put(blackboardKey, foundMember.get());
            return success();
        }

        return failure();
    }

    @Override
    public void reset() {
        // No state
    }

    @Override
    public String getCategory() {
        return "Action";
    }
}
