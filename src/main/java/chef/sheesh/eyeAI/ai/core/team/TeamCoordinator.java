package chef.sheesh.eyeAI.ai.core.team;

import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all AI teams in the system.
 * This class is the central point for creating, managing, and coordinating teams.
 */
public class TeamCoordinator {

    private final Map<UUID, Team> teams;
    private final Map<IFakePlayer, Team> playerTeamMap;

    public TeamCoordinator() {
        this.teams = new ConcurrentHashMap<>();
        this.playerTeamMap = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new team with the given leader.
     *
     * @param leader The leader of the new team.
     * @return The newly created team.
     */
    public Team createTeam(IFakePlayer leader) {
        Team team = new Team(leader);
        teams.put(team.getTeamId(), team);
        playerTeamMap.put(leader, team);
        leader.setTeam(team);
        leader.setRole(TeamRole.LEADER);
        return team;
    }

    /**
     * Disbands a team.
     *
     * @param teamId The ID of the team to disband.
     */
    public void disbandTeam(UUID teamId) {
        Team team = teams.remove(teamId);
        if (team != null) {
            for (IFakePlayer member : team.getMembers().keySet()) {
                playerTeamMap.remove(member);
                member.setTeam(null);
                member.setRole(TeamRole.NONE);
            }
        }
    }

    /**
     * Adds a member to a team.
     *
     * @param member The member to add.
     * @param team The team to join.
     * @param role The role of the new member.
     */
    public void addMemberToTeam(IFakePlayer member, Team team, TeamRole role) {
        // A player can only be in one team at a time.
        if (playerTeamMap.containsKey(member)) {
            removeMemberFromCurrentTeam(member);
        }
        team.addMember(member, role);
        playerTeamMap.put(member, team);
        member.setTeam(team);
        member.setRole(role);
    }

    /**
     * Removes a member from their current team.
     *
     * @param member The member to remove.
     */
    public void removeMemberFromCurrentTeam(IFakePlayer member) {
        Team team = playerTeamMap.remove(member);
        if (team != null) {
            team.removeMember(member);
            member.setTeam(null);
            member.setRole(TeamRole.NONE);
            if (team.getMembers().isEmpty()) {
                disbandTeam(team.getTeamId());
            }
        }
    }

    /**
     * Gets the team of a specific player.
     *
     * @param player The player.
     * @return The team of the player, or null if not in a team.
     */
    public Team getTeam(IFakePlayer player) {
        return playerTeamMap.get(player);
    }

    /**
     * Gets a team by its ID.
     *
     * @param teamId The ID of the team.
     * @return The team, or null if not found.
     */
    public Team getTeam(UUID teamId) {
        return teams.get(teamId);
    }
}
