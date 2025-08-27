package chef.sheesh.eyeAI.ai.behavior;

import chef.sheesh.eyeAI.ai.behavior.nodes.*;
import chef.sheesh.eyeAI.ai.core.DecisionContext;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * Modern factory for creating behavior tree configurations.
 * Provides pre-built trees for common AI behaviors.
 */
public class BehaviorTreeFactory {

    /**
     * Create a default combat behavior tree
     * This tree handles basic combat: patrol until enemy spotted, then attack
     */
    public IBehaviorTree createDefaultCombatTree() {
        // Combat sequence: if enemy nearby, attack; otherwise patrol
        SequenceNode combatSequence = new SequenceNode(
            new HasTargetCondition(),
            new AttackNode(null) // Target will be set by context
        );

        // Patrol action (placeholder - would need a patrol implementation)
        IBehaviorTree patrolAction = new BehaviorTree() {
            @Override
            public ExecutionResult execute(IFakePlayer fakePlayer) {
                // Simple idle behavior
                return success();
            }

            @Override
            public void reset() {
                markNotRunning();
            }

            @Override
            public String getDescription() {
                return "Patrol";
            }

            @Override
            public String getCategory() {
                return "Action";
            }
        };

        // Main behavior: try combat first, fallback to patrol
        SelectorNode root = new SelectorNode(
            combatSequence,
            patrolAction
        );
        root.setName("DefaultCombat");
        return root;
    }

    /**
     * Create a simple patrol behavior tree
     */
    public IBehaviorTree createPatrolTree() {
        // Create a basic patrol around the spawn area
        BehaviorTree patrolTree = new BehaviorTree() {
            private chef.sheesh.eyeAI.ai.behavior.nodes.PatrolNode patrolNode = null;
            private long lastPatrolUpdate = 0;

            @Override
            public ExecutionResult execute(IFakePlayer fakePlayer) {
                if (!(fakePlayer instanceof chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer)) {
                    return failure();
                }

                chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer realFakePlayer = (chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer) fakePlayer;

                // Initialize patrol node if needed
                if (patrolNode == null) {
                    patrolNode = createBasicPatrol(realFakePlayer.getLocation());
                }

                // Update patrol waypoints periodically
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastPatrolUpdate > 60000) { // Update every minute
                    patrolNode = createBasicPatrol(realFakePlayer.getLocation());
                    lastPatrolUpdate = currentTime;
                }

                return patrolNode.execute(fakePlayer);
            }

            private chef.sheesh.eyeAI.ai.behavior.nodes.PatrolNode createBasicPatrol(org.bukkit.Location center) {
                java.util.List<org.bukkit.Location> waypoints = new java.util.ArrayList<>();
                java.util.Random random = new java.util.Random();

                // Create 4-6 random waypoints around the center
                int waypointCount = 4 + random.nextInt(3);
                for (int i = 0; i < waypointCount; i++) {
                    double angle = (2 * Math.PI * i) / waypointCount;
                    double distance = 5 + random.nextDouble() * 10; // 5-15 blocks away
                    double x = center.getX() + distance * Math.cos(angle);
                    double z = center.getZ() + distance * Math.sin(angle);
                    org.bukkit.Location waypoint = new org.bukkit.Location(center.getWorld(), x, center.getY(), z);
                    waypoints.add(waypoint);
                }

                return new chef.sheesh.eyeAI.ai.behavior.nodes.PatrolNode(waypoints, 0.8, 4000, true,
                    chef.sheesh.eyeAI.ai.behavior.nodes.PatrolNode.PatrolPattern.CIRCULAR);
            }

            @Override
            public void reset() {
                if (patrolNode != null) {
                    patrolNode.reset();
                }
                lastPatrolUpdate = 0;
                markNotRunning();
            }

            @Override
            public String getDescription() {
                return "Dynamic Patrol";
            }

            @Override
            public String getCategory() {
                return "Movement";
            }
        };
        patrolTree.setName("Patrol");
        return patrolTree;
    }

    /**
     * Create a flee behavior tree for when health is low
     */
    public IBehaviorTree createFleeTree() {
        // Create flee node with appropriate parameters
        FleeNode fleeAction = new FleeNode(12.0, 18.0, 2.2, 10000, true);
        fleeAction.setName("Flee");

        SequenceNode fleeTree = new SequenceNode(
            new HealthLowCondition(),
            new TimeoutDecorator(fleeAction, 10000) // Timeout after 10 seconds
        );
        fleeTree.setName("FleeSequence");
        return fleeTree;
    }

    /**
     * Create a complex combat tree with flee behavior and modern features
     */
    public IBehaviorTree createAdvancedCombatTree() {
        // Flee behavior with timeout
        BehaviorTree fleeAction = new BehaviorTree() {
            @Override
            public ExecutionResult execute(IFakePlayer fakePlayer) {
                if (!(fakePlayer instanceof FakePlayer)) {
                    return failure();
                }

                FakePlayer realFakePlayer = (FakePlayer) fakePlayer;
                realFakePlayer.setState(FakePlayerState.FLEEING);
                // TODO: Implement actual flee movement
                return running();
            }

            @Override
            public void reset() {
                markNotRunning();
            }

            @Override
            public String getDescription() {
                return "FleeAction";
            }

            @Override
            public String getCategory() {
                return "Action";
            }
        };
        fleeAction.setName("Flee");

        SequenceNode fleeSequence = new SequenceNode(
            new HealthLowCondition(),
            new TimeoutDecorator(fleeAction, 15000) // 15 second timeout
        );
        fleeSequence.setName("FleeSequence");

        // Combat sequence with movement and retry logic
        SequenceNode combatSequence = new SequenceNode(
            new HasTargetCondition(),
            new RepeatDecorator(new AttackNode(null), 5) // Try attacking up to 5 times
        );
        combatSequence.setName("CombatSequence");

        // Idle action with periodic checks
        BehaviorTree idleAction = new BehaviorTree() {
            private long lastCheckTime = 0;

            @Override
            public ExecutionResult execute(IFakePlayer fakePlayer) {
                if (!(fakePlayer instanceof FakePlayer)) {
                    return failure();
                }

                FakePlayer realFakePlayer = (FakePlayer) fakePlayer;
                realFakePlayer.setState(FakePlayerState.IDLE);

                // Check for targets every 5 seconds while idle
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastCheckTime > 5000) {
                    lastCheckTime = currentTime;
                    // TODO: Implement target scanning
                }

                return success();
            }

            @Override
            public void reset() {
                markNotRunning();
                lastCheckTime = 0;
            }

            @Override
            public String getDescription() {
                return "Idle";
            }

            @Override
            public String getCategory() {
                return "Action";
            }
        };
        idleAction.setName("Idle");

        // Main behavior: flee if low health, fight if enemy nearby, otherwise idle
        SelectorNode root = new SelectorNode(
            fleeSequence,
            combatSequence,
            idleAction
        );
        root.setName("AdvancedCombat");
        return root;
    }

    /**
     * Create a parallel behavior tree that handles multiple tasks concurrently
     */
    public IBehaviorTree createParallelCombatTree() {
        // Movement towards target
        BehaviorTree moveToTarget = new BehaviorTree() {
            private MoveToNode moveNode = null;
            private org.bukkit.Location lastTargetLocation = null;

            @Override
            public ExecutionResult execute(IFakePlayer fakePlayer) {
                if (!(fakePlayer instanceof FakePlayer)) {
                    return failure();
                }

                FakePlayer realFakePlayer = (FakePlayer) fakePlayer;
                DecisionContext context = realFakePlayer.createDecisionContext();

                if (!context.getCurrentTarget().isPresent()) {
                    return failure();
                }

                org.bukkit.entity.Entity target = context.getCurrentTarget().get();

                // Create or update movement node
                if (moveNode == null || lastTargetLocation == null || !lastTargetLocation.equals(target.getLocation())) {
                    moveNode = new MoveToNode(null, target.getLocation(), 1.5, 2.0, false);
                    lastTargetLocation = target.getLocation().clone();
                }

                return moveNode.execute(fakePlayer);
            }

            @Override
            public void reset() {
                if (moveNode != null) {
                    moveNode.reset();
                    moveNode = null;
                }
                lastTargetLocation = null;
                markNotRunning();
            }

            @Override
            public String getDescription() {
                return "MoveToTarget";
            }

            @Override
            public String getCategory() {
                return "Movement";
            }
        };
        moveToTarget.setName("MoveToTarget");

        BehaviorTree scanForThreats = new BehaviorTree() {
            private long lastScanTime = 0;

            @Override
            public ExecutionResult execute(IFakePlayer fakePlayer) {
                if (!(fakePlayer instanceof FakePlayer)) {
                    return failure();
                }

                FakePlayer realFakePlayer = (FakePlayer) fakePlayer;
                long currentTime = System.currentTimeMillis();

                // Rate limit scanning
                if (currentTime - lastScanTime < 2000) { // Scan every 2 seconds
                    return running();
                }

                DecisionContext context = realFakePlayer.createDecisionContext();

                // Check for nearby threats
                if (context.hasHostileNearby()) {
                    // Found threats, scanning successful
                    lastScanTime = currentTime;
                    return success();
                }

                // No threats found, but scanning is still running
                lastScanTime = currentTime;
                return running();
            }

            @Override
            public void reset() {
                lastScanTime = 0;
                markNotRunning();
            }

            @Override
            public String getDescription() {
                return "ScanThreats";
            }

            @Override
            public String getCategory() {
                return "Perception";
            }
        };
        scanForThreats.setName("ScanThreats");

        // Parallel node: succeed if movement succeeds, fail if scanning fails
        ParallelNode parallelTasks = new ParallelNode(
            new IBehaviorTree[]{moveToTarget, scanForThreats},
            ParallelNode.Policy.REQUIRE_ONE,  // Succeed if at least one succeeds
            ParallelNode.Policy.REQUIRE_ONE   // Fail if at least one fails
        );
        parallelTasks.setName("ParallelTasks");

        // Main sequence: scan first, then parallel movement and combat
        SequenceNode root = new SequenceNode(
            new HasTargetCondition(),
            parallelTasks,
            new AttackNode(null)
        );
        root.setName("ParallelCombat");
        return root;
    }

    /**
     * Create a tree with dynamic priority selection
     */
    public IBehaviorTree createDynamicPriorityTree() {
        // High priority: flee when health is critical
        SequenceNode criticalFlee = new SequenceNode(
            new InvertDecorator(new HealthLowCondition()), // NOT health low = health critical
            new TimeoutDecorator(createFleeTree(), 20000)  // 20 second timeout
        );
        criticalFlee.setName("CriticalFlee");

        // Medium priority: attack if target available
        SequenceNode attackPriority = new SequenceNode(
            new HasTargetCondition(),
            new RepeatDecorator(new AttackNode(null), 3)
        );
        attackPriority.setName("AttackPriority");

        // Low priority: patrol
        SequenceNode patrolPriority = new SequenceNode(
            new InvertDecorator(new HasTargetCondition()), // No target available
            createPatrolTree()
        );
        patrolPriority.setName("PatrolPriority");

        // Dynamic priority selector
        SelectorNode root = new SelectorNode(
            criticalFlee,
            attackPriority,
            patrolPriority
        );
        root.setName("DynamicPriority");
        return root;
    }

    /**
     * Create a utility method for building custom trees
     */
    public static class TreeBuilder {
        private final SelectorNode root;

        public TreeBuilder(String name) {
            this.root = new SelectorNode();
            this.root.setName(name);
        }

        public TreeBuilder addHighPriority(IBehaviorTree behavior) {
            root.addChild(behavior);
            return this;
        }

        public TreeBuilder addLowPriority(IBehaviorTree behavior) {
            root.addChild(behavior);
            return this;
        }

        public IBehaviorTree build() {
            return root;
        }
    }

    /**
     * Create a custom tree using the builder pattern
     */
    public IBehaviorTree createCustomTree(String name) {
        return new TreeBuilder(name)
            .addHighPriority(createFleeTree())
            .addHighPriority(new SequenceNode(
                new HasTargetCondition(),
                new AttackNode(null)
            ))
            .addLowPriority(createPatrolTree())
            .build();
    }
}
