package chef.sheesh.eyeAI.ai.core.team;

import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a team of AI agents.
 * A team has a leader and members with different roles.
 */
public class Team {

    private final UUID teamId;
    private final Map<IFakePlayer, TeamRole> members;
    private IFakePlayer leader;

    public Team(IFakePlayer leader) {
        this.teamId = UUID.randomUUID();
        this.members = new ConcurrentHashMap<>();
        this.leader = leader;
        addMember(leader, TeamRole.LEADER);
    }

    public UUID getTeamId() {
        return teamId;
    }

    public IFakePlayer getLeader() {
        return leader;
    }

    public void setLeader(IFakePlayer leader) {
        // Ensure the new leader is a member of the team.
        if (members.containsKey(leader)) {
            this.leader = leader;
            setRole(leader, TeamRole.LEADER);
        }
    }

    public void addMember(IFakePlayer member, TeamRole role) {
        members.put(member, role);
    }

    public void removeMember(IFakePlayer member) {
        members.remove(member);
        if (leader.equals(member)) {
            // If the leader is removed, assign a new leader.
            if (!members.isEmpty()) {
                setLeader(members.keySet().iterator().next());
            } else {
                leader = null;
            }
        }
    }

    public Map<IFakePlayer, TeamRole> getMembers() {
        return members;
    }

    public TeamRole getRole(IFakePlayer member) {
        return members.get(member);
    }

    public void setRole(IFakePlayer member, TeamRole role) {
        if (members.containsKey(member)) {
            members.put(member, role);
        }
    }
}
