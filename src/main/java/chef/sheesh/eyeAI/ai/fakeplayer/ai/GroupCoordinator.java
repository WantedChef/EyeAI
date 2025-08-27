package chef.sheesh.eyeAI.ai.fakeplayer.ai;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Group coordination system for fake players to work together
 */
public class GroupCoordinator {

    private final Map<UUID, Group> groups;
    private final Map<UUID, GroupAssignment> assignments;

    public GroupCoordinator() {
        this.groups = new ConcurrentHashMap<>();
        this.assignments = new ConcurrentHashMap<>();
    }

    /**
     * Create a new group
     */
    public Group createGroup(String name, Location center, GroupRole leaderRole) {
        UUID groupId = UUID.randomUUID();
        Group group = new Group(groupId, name, center, leaderRole);
        groups.put(groupId, group);
        return group;
    }

    /**
     * Assign a fake player to a group
     */
    public boolean assignToGroup(FakePlayer fakePlayer, UUID groupId, GroupRole role) {
        Group group = groups.get(groupId);
        if (group == null || group.getMembers().size() >= group.getMaxSize()) {
            return false;
        }

        // Remove from existing group
        unassignFromGroup(fakePlayer.getId());

        // Assign to new group
        group.addMember(fakePlayer, role);
        assignments.put(fakePlayer.getId(), new GroupAssignment(groupId, role));

        return true;
    }

    /**
     * Remove fake player from their group
     */
    public void unassignFromGroup(UUID fakePlayerId) {
        GroupAssignment assignment = assignments.remove(fakePlayerId);
        if (assignment != null) {
            Group group = groups.get(assignment.getGroupId());
            if (group != null) {
                group.removeMember(fakePlayerId);
            }
        }
    }

    /**
     * Get group for a fake player
     */
    public Optional<Group> getGroup(UUID fakePlayerId) {
        GroupAssignment assignment = assignments.get(fakePlayerId);
        if (assignment != null) {
            return Optional.ofNullable(groups.get(assignment.getGroupId()));
        }
        return Optional.empty();
    }

    /**
     * Get group by ID
     */
    public Optional<Group> getGroupById(UUID groupId) {
        return Optional.ofNullable(groups.get(groupId));
    }

    /**
     * Get the role of a fake player in their group
     */
    public Optional<GroupRole> getPlayerRole(UUID fakePlayerId) {
        GroupAssignment assignment = assignments.get(fakePlayerId);
        if (assignment != null) {
            return Optional.of(assignment.getRole());
        }
        return Optional.empty();
    }

    /**
     * Update group coordination
     */
    public void updateCoordination() {
        for (Group group : groups.values()) {
            updateGroupBehavior(group);
        }
    }

    /**
     * Update behavior for a specific group
     */
    private void updateGroupBehavior(Group group) {
        if (group.getMembers().isEmpty()) {
            return;
        }

        // Update group formation
        updateFormation(group);

        // Coordinate combat
        coordinateCombat(group);

        // Coordinate movement
        coordinateMovement(group);

        // Update group objectives
        updateObjectives(group);
    }

    /**
     * Update group formation
     */
    private void updateFormation(Group group) {
        Location center = group.getCenter();
        List<Map.Entry<FakePlayer, GroupRole>> members = new ArrayList<>(group.getMembers().entrySet());

        for (int i = 0; i < members.size(); i++) {
            FakePlayer member = members.get(i).getKey();
            GroupRole role = members.get(i).getValue();

            // Calculate position in formation
            Location targetPos = calculateFormationPosition(center, i, members.size(), role);

            // Move towards formation position if too far
            if (member.getLocation().distance(targetPos) > 3.0) {
                // Use pathfinding to move to position - integrated with PathFinder
                moveToFormationPosition(member, targetPos);
            }
        }
    }

    /**
     * Move member to formation position
     */
    private void moveToFormationPosition(FakePlayer member, Location targetPos) {
        // Safely call pathfinder and movement controller with null checks
        if (member.getPathfinder() != null) {
            member.getPathfinder().findPath(targetPos);
        }
        if (member.getMovementController() != null) {
            member.getMovementController().moveTowards(targetPos);
        }
    }

    /**
     * Calculate position in formation
     */
    private Location calculateFormationPosition(Location center, int index, int totalMembers, GroupRole role) {
        double radius = 3.0;
        double angle = (2 * Math.PI * index) / totalMembers;

        // Adjust radius based on role
        if (role == GroupRole.SCOUT) {
            radius *= 1.5;
        } else if (role == GroupRole.GUARD) {
            radius *= 0.7;
        }

        double x = center.getX() + radius * Math.cos(angle);
        double z = center.getZ() + radius * Math.sin(angle);

        return new Location(center.getWorld(), x, center.getY(), z);
    }

    /**
     * Coordinate combat within the group
     */
    private void coordinateCombat(Group group) {
        // Find enemies threatening the group
        Set<Entity> groupThreats = findGroupThreats(group);

        if (!groupThreats.isEmpty()) {
            // Assign targets to group members
            assignCombatTargets(group, groupThreats);

            // Coordinate attack patterns
            coordinateAttackPattern(group, groupThreats);
        }
    }

    /**
     * Find threats to the entire group
     */
    private Set<Entity> findGroupThreats(Group group) {
        Set<Entity> threats = new HashSet<>();
        Location center = group.getCenter();
        double threatRange = 20.0;

        // Check for threats around group center
        for (Entity entity : center.getWorld().getNearbyEntities(center, threatRange, threatRange, threatRange)) {
            if (isHostile(entity)) {
                threats.add(entity);
            }
        }

        return threats;
    }

    /**
     * Assign combat targets to group members
     */
    private void assignCombatTargets(Group group, Set<Entity> threats) {
        List<Entity> threatList = new ArrayList<>(threats);
        List<FakePlayer> attackers = group.getMembers().entrySet().stream()
            .filter(entry -> entry.getValue() != GroupRole.HEALER && entry.getValue() != GroupRole.SUPPORT)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        // Assign targets to attackers
        assignTargetsToAttackers(attackers, threatList);
    }

    /**
     * Assign targets to attackers safely
     */
    private void assignTargetsToAttackers(List<FakePlayer> attackers, List<Entity> threatList) {
        for (int i = 0; i < attackers.size() && i < threatList.size(); i++) {
            FakePlayer attacker = attackers.get(i);
            Entity target = threatList.get(i);

            // Set target for attacker - integrated with TargetSelector
            setAttackerTarget(attacker, target);
        }
    }

    /**
     * Set target for attacker with null checks
     */
    private void setAttackerTarget(FakePlayer attacker, Entity target) {
        if (attacker.getTargetSelector() != null) {
            attacker.getTargetSelector().setTarget(target);
        }
        if (attacker.getCombatController() != null) {
            attacker.getCombatController().engageTarget(target);
        }
    }

    /**
     * Coordinate attack patterns
     */
    private void coordinateAttackPattern(Group group, Set<Entity> threats) {
        // Implement flanking, pincer, or other coordinated attack patterns
        if (threats.size() == 1) {
            // Surround single target
            implementSurroundPattern(group, threats.iterator().next());
        } else if (threats.size() > 1) {
            // Divide and conquer
            implementDivideAndConquerPattern(group, threats);
        }
    }

    /**
     * Implement surround pattern for single target
     */
    private void implementSurroundPattern(Group group, Entity target) {
        List<FakePlayer> flankers = group.getMembers().keySet().stream().toList();

        executeSurroundMovement(flankers, target);
    }

    /**
     * Execute surround movement for flankers
     */
    private void executeSurroundMovement(List<FakePlayer> flankers, Entity target) {
        for (int i = 0; i < flankers.size(); i++) {
            // Move flanker to flanking position - integrated with movement system
            Location targetLoc = target.getLocation();
            double angle = (2 * Math.PI * i) / flankers.size();
            double distance = 5.0;
            double x = targetLoc.getX() + distance * Math.cos(angle);
            double z = targetLoc.getZ() + distance * Math.sin(angle);
            Location flankPos = new Location(targetLoc.getWorld(), x, targetLoc.getY(), z);

            FakePlayer flanker = flankers.get(i);
            moveToFlankPosition(flanker, flankPos, target);
        }
    }

    /**
     * Move flanker to flank position
     */
    private void moveToFlankPosition(FakePlayer flanker, Location flankPos, Entity target) {
        if (flanker.getPathfinder() != null) {
            flanker.getPathfinder().findPath(flankPos);
        }
        if (flanker.getMovementController() != null) {
            flanker.getMovementController().moveTowards(flankPos);
        }
        if (flanker.getTargetSelector() != null) {
            flanker.getTargetSelector().setTarget(target);
        }
    }

    /**
     * Implement divide and conquer pattern
     */
    private void implementDivideAndConquerPattern(Group group, Set<Entity> threats) {
        List<Entity> threatList = new ArrayList<>(threats);
        List<FakePlayer> attackers = new ArrayList<>(group.getMembers().keySet());

        // Assign each attacker to a different threat
        executeDivideAndConquer(attackers, threatList);
    }

    /**
     * Execute divide and conquer strategy
     */
    private void executeDivideAndConquer(List<FakePlayer> attackers, List<Entity> threatList) {
        for (int i = 0; i < attackers.size() && i < threatList.size(); i++) {
            FakePlayer attacker = attackers.get(i);
            Entity threat = threatList.get(i);

            // Focus each attacker on a different target
            assignAttackerToThreat(attacker, threat);
        }
    }

    /**
     * Assign attacker to specific threat
     */
    private void assignAttackerToThreat(FakePlayer attacker, Entity threat) {
        if (attacker.getTargetSelector() != null) {
            attacker.getTargetSelector().setTarget(threat);
        }
        if (attacker.getCombatController() != null) {
            attacker.getCombatController().engageTarget(threat);
        }

        // Position attacker optimally for their assigned threat
        Location threatLoc = threat.getLocation();
        Location approachPos = calculateOptimalAttackPosition(threatLoc, attacker.getLocation());

        if (attacker.getPathfinder() != null) {
            attacker.getPathfinder().findPath(approachPos);
        }
        if (attacker.getMovementController() != null) {
            attacker.getMovementController().moveTowards(approachPos);
        }
    }

    /**
     * Calculate optimal attack position relative to target
     */
    private Location calculateOptimalAttackPosition(Location targetLoc, Location currentLoc) {
        // Calculate position 3 blocks away from target, in direction of current position
        double distance = 3.0;
        double dx = currentLoc.getX() - targetLoc.getX();
        double dz = currentLoc.getZ() - targetLoc.getZ();
        double magnitude = Math.sqrt(dx * dx + dz * dz);

        if (magnitude == 0) {
            // If same position, choose random direction
            double angle = Math.random() * 2 * Math.PI;
            dx = Math.cos(angle);
            dz = Math.sin(angle);
        } else {
            dx /= magnitude;
            dz /= magnitude;
        }

        return new Location(targetLoc.getWorld(),
            targetLoc.getX() + dx * distance,
            targetLoc.getY(),
            targetLoc.getZ() + dz * distance);
    }

    /**
     * Coordinate movement within the group
     */
    private void coordinateMovement(Group group) {
        // Keep group together
        maintainGroupCohesion(group);

        // Avoid splitting the group
        preventGroupSplitting(group);
    }

    /**
     * Maintain group cohesion
     */
    private void maintainGroupCohesion(Group group) {
        Location center = calculateGroupCenter(group);
        group.setCenter(center);

        // Move stragglers towards center
        moveStragglersToCenter(group, center);
    }

    /**
     * Move stragglers towards center
     */
    private void moveStragglersToCenter(Group group, Location center) {
        for (Map.Entry<FakePlayer, GroupRole> entry : group.getMembers().entrySet()) {
            FakePlayer member = entry.getKey();
            Location memberLoc = member.getLocation();

            if (memberLoc.distance(center) > group.getMaxSpread()) {
                // Move towards center - integrated with movement system
                Location targetPos = calculateFormationPosition(center, 0, 1, entry.getValue());
                moveToFormationPosition(member, targetPos);

                if (member.getMovementController() != null) {
                    member.getMovementController().setUrgent(true); // High priority movement
                }
            }
        }
    }

    /**
     * Prevent group from splitting
     */
    private void preventGroupSplitting(Group group) {
        // Identify isolated members
        identifyAndReuniteIsolatedMembers(group);
    }

    /**
     * Identify and reunite isolated members
     */
    private void identifyAndReuniteIsolatedMembers(Group group) {
        for (Map.Entry<FakePlayer, GroupRole> entry : group.getMembers().entrySet()) {
            FakePlayer member = entry.getKey();
            int nearbyMembers = countNearbyGroupMembers(member, group);

            if (nearbyMembers == 0) {
                // Member is isolated, move towards group center - integrated with movement system
                reuniteIsolatedMember(group, member);
            }
        }
    }

    /**
     * Reunite isolated member with group
     */
    private void reuniteIsolatedMember(Group group, FakePlayer member) {
        Location groupCenter = calculateGroupCenter(group);

        if (member.getPathfinder() != null) {
            member.getPathfinder().findPath(groupCenter);
        }
        if (member.getMovementController() != null) {
            member.getMovementController().moveTowards(groupCenter);
            member.getMovementController().setUrgent(true); // High priority to rejoin group
        }

        // Also signal other members to wait or slow down
        signalGroupToWaitForMember(group, member);
    }

    /**
     * Signal group members to wait for isolated member
     */
    private void signalGroupToWaitForMember(Group group, FakePlayer isolatedMember) {
        for (FakePlayer member : group.getMembers().keySet()) {
            if (member != isolatedMember && member.getMovementController() != null) {
                member.getMovementController().reduceSpeed(0.5); // Slow down to 50% speed
                member.getMovementController().setWaitingForGroup(true);
            }
        }
    }

    /**
     * Update group objectives
     */
    private void updateObjectives(Group group) {
        // Update group objectives based on current situation
        Set<Entity> threats = findGroupThreats(group);

        updateObjectiveBasedOnThreats(group, threats);

        // Update individual member objectives based on group objective
        updateMemberObjectives(group);
    }

    /**
     * Update objective based on threats
     */
    private void updateObjectiveBasedOnThreats(Group group, Set<Entity> threats) {
        if (!threats.isEmpty()) {
            // Switch to combat mode if threats detected
            group.setObjective(GroupObjective.COMBAT);
            coordinateCombatFormation(group, threats);
        } else {
            // Return to patrol or previous objective
            if (group.getObjective() == GroupObjective.COMBAT) {
                group.setObjective(GroupObjective.PATROL);
                coordinatePatrolFormation(group);
            }
        }
    }

    /**
     * Coordinate combat formation
     */
    private void coordinateCombatFormation(Group group, Set<Entity> threats) {
        // Arrange members in combat formation
        Entity primaryThreat = threats.iterator().next();
        Location threatLoc = primaryThreat.getLocation();

        arrangeCombatFormation(group, threatLoc);
    }

    /**
     * Arrange combat formation
     */
    private void arrangeCombatFormation(Group group, Location threatLoc) {
        for (Map.Entry<FakePlayer, GroupRole> entry : group.getMembers().entrySet()) {
            FakePlayer member = entry.getKey();
            GroupRole role = entry.getValue();

            Location combatPos = calculateCombatPosition(threatLoc, role, group.getMembers().size());

            if (member.getPathfinder() != null) {
                member.getPathfinder().findPath(combatPos);
            }
            if (member.getMovementController() != null) {
                member.getMovementController().moveTowards(combatPos);
            }
        }
    }

    /**
     * Calculate combat position based on role
     */
    private Location calculateCombatPosition(Location threatLoc, GroupRole role, int groupSize) {
        double distance = switch (role) {
            case LEADER, ATTACKER -> 4.0; // Close combat range
            case GUARD -> 3.0; // Protective range
            case SCOUT -> 8.0; // Observation range
            case HEALER, SUPPORT -> 6.0; // Safe support range
        };

        // Random angle to spread around threat
        double angle = Math.random() * 2 * Math.PI;
        double x = threatLoc.getX() + distance * Math.cos(angle);
        double z = threatLoc.getZ() + distance * Math.sin(angle);

        return new Location(threatLoc.getWorld(), x, threatLoc.getY(), z);
    }

    /**
     * Coordinate patrol formation
     */
    private void coordinatePatrolFormation(Group group) {
        // Arrange members in patrol formation
        Location center = group.getCenter();
        List<Map.Entry<FakePlayer, GroupRole>> members = new ArrayList<>(group.getMembers().entrySet());

        arrangePatrolFormation(members, center);
    }

    /**
     * Arrange patrol formation
     */
    private void arrangePatrolFormation(List<Map.Entry<FakePlayer, GroupRole>> members, Location center) {
        for (int i = 0; i < members.size(); i++) {
            FakePlayer member = members.get(i).getKey();
            GroupRole role = members.get(i).getValue();

            Location patrolPos = calculateFormationPosition(center, i, members.size(), role);

            if (member.getPathfinder() != null) {
                member.getPathfinder().findPath(patrolPos);
            }
            if (member.getMovementController() != null) {
                member.getMovementController().moveTowards(patrolPos);
            }
        }
    }

    /**
     * Update individual member objectives
     */
    private void updateMemberObjectives(Group group) {
        for (Map.Entry<FakePlayer, GroupRole> entry : group.getMembers().entrySet()) {
            FakePlayer member = entry.getKey();

            updateMemberBehavior(member, group.getObjective());
        }
    }

    /**
     * Update member behavior based on objective
     */
    private void updateMemberBehavior(FakePlayer member, GroupObjective objective) {
        if (member.getBehaviorController() != null) {
            switch (objective) {
                case COMBAT -> member.getBehaviorController().setCombatMode(true);
                case PATROL -> member.getBehaviorController().setPatrolMode(true);
                case EXPLORE -> member.getBehaviorController().setExploreMode(true);
                case DEFEND -> member.getBehaviorController().setDefendMode(true);
                case ESCORT -> member.getBehaviorController().setEscortMode(true);
            }
        }
    }

    /**
     * Count nearby group members - optimized to remove always constant parameter warning
     */
    private int countNearbyGroupMembers(FakePlayer member, Group group) {
        return countNearbyGroupMembers(member, group, 10.0);
    }

    /**
     * Count nearby group members with configurable range
     */
    private int countNearbyGroupMembers(FakePlayer member, Group group, double range) {
        Location memberLoc = member.getLocation();
        int count = 0;

        for (FakePlayer otherMember : group.getMembers().keySet()) {
            if (otherMember != member && otherMember.getLocation().distance(memberLoc) <= range) {
                count++;
            }
        }

        return count;
    }

    /**
     * Calculate center of the group
     */
    private Location calculateGroupCenter(Group group) {
        if (group.getMembers().isEmpty()) {
            return group.getCenter();
        }

        double totalX = 0, totalY = 0, totalZ = 0;
        int count = 0;

        for (FakePlayer member : group.getMembers().keySet()) {
            Location loc = member.getLocation();
            totalX += loc.getX();
            totalY += loc.getY();
            totalZ += loc.getZ();
            count++;
        }

        return new Location(
            group.getCenter().getWorld(),
            totalX / count,
            totalY / count,
            totalZ / count
        );
    }

    /**
     * Check if entity is hostile
     */
    private boolean isHostile(Entity entity) {
        String entityType = entity.getType().name();
        return entityType.contains("ZOMBIE") || entityType.contains("SKELETON") ||
               entityType.contains("CREEPER") || entityType.contains("SPIDER") ||
               entityType.contains("ENDERMAN") || entityType.contains("BLAZE");
    }

    /**
     * Get all groups
     */
    public Collection<Group> getAllGroups() {
        return groups.values();
    }

    /**
     * Remove empty groups
     */
    public void cleanupEmptyGroups() {
        groups.entrySet().removeIf(entry -> entry.getValue().getMembers().isEmpty());
    }

    /**
     * Group data class
     */
    public static class Group {
        private final UUID id;
        private final String name;
        private Location center;
        private final Map<UUID, Map.Entry<FakePlayer, GroupRole>> members;
        private final GroupRole leaderRole;
        private int maxSize = 8;
        private double maxSpread = 15.0;
        private GroupObjective objective = GroupObjective.PATROL;

        public Group(UUID id, String name, Location center, GroupRole leaderRole) {
            this.id = id;
            this.name = name;
            this.center = center.clone();
            this.members = new ConcurrentHashMap<>();
            this.leaderRole = leaderRole;
        }

        public UUID getId() { return id; }
        public String getName() { return name; }
        public Location getCenter() { return center.clone(); }
        public void setCenter(Location center) { this.center = center.clone(); }
        public Map<FakePlayer, GroupRole> getMembers() {
            return members.values().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
        }
        public GroupRole getLeaderRole() { return leaderRole; }
        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
        public double getMaxSpread() { return maxSpread; }
        public void setMaxSpread(double maxSpread) { this.maxSpread = maxSpread; }
        public GroupObjective getObjective() { return objective; }
        public void setObjective(GroupObjective objective) { this.objective = objective; }

        public void addMember(FakePlayer fakePlayer, GroupRole role) {
            members.put(fakePlayer.getId(), Map.entry(fakePlayer, role));
        }

        public void removeMember(UUID fakePlayerId) {
            members.remove(fakePlayerId);
        }
    }

    /**
     * Group assignment data
     */
    private static class GroupAssignment {
        private final UUID groupId;
        private final GroupRole role;

        public GroupAssignment(UUID groupId, GroupRole role) {
            this.groupId = groupId;
            this.role = role;
        }

        public UUID getGroupId() { return groupId; }
        public GroupRole getRole() { return role; }
    }

    /**
     * Group roles
     */
    public enum GroupRole {
        LEADER,     // Group leader
        SCOUT,      // Scout ahead
        GUARD,      // Protect the group
        ATTACKER,   // Main combat
        HEALER,     // Support/healing
        SUPPORT     // Utility/support
    }

    /**
     * Group objectives
     */
    public enum GroupObjective {
        PATROL,     // Patrol area
        COMBAT,     // Engage in combat
        EXPLORE,    // Explore new areas
        DEFEND,     // Defend position
        ESCORT      // Escort something/someone
    }
}
