package chef.sheesh.eyeAI.ai.behavior;

import chef.sheesh.eyeAI.ai.behavior.nodes.*;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * Demonstration class showcasing the upgraded behavior tree system.
 * This class shows all the new features and capabilities.
 */
public class BehaviorTreeDemo {

    private final BehaviorTreeFactory factory;

    public BehaviorTreeDemo() {
        this.factory = new BehaviorTreeFactory();
    }

    /**
     * Demonstrate all the new features of the upgraded behavior tree system
     */
    public void runFullDemo() {
        System.out.println("=== Behavior Tree Upgrade Demo ===\n");

        // 1. Basic tree demonstration
        demonstrateBasicTrees();

        // 2. Decorator nodes
        demonstrateDecorators();

        // 3. Parallel execution
        demonstrateParallelExecution();

        // 4. Tree visualization
        demonstrateVisualization();

        // 5. Advanced tree building
        demonstrateAdvancedBuilding();
    }

    private void demonstrateBasicTrees() {
        System.out.println("1. BASIC TREE DEMONSTRATION");
        System.out.println("===========================\n");

        IBehaviorTree basicCombat = factory.createDefaultCombatTree();
        System.out.println("Default Combat Tree:");
        System.out.println(basicCombat.getDescription());
        System.out.println();

        IBehaviorTree advancedCombat = factory.createAdvancedCombatTree();
        System.out.println("Advanced Combat Tree:");
        System.out.println(advancedCombat.getDescription());
        System.out.println();
    }

    private void demonstrateDecorators() {
        System.out.println("2. DECORATOR NODES");
        System.out.println("==================\n");

        // Invert decorator example
        ConditionNode healthLow = new HasTargetCondition();
        InvertDecorator notHealthLow = new InvertDecorator(healthLow);
        System.out.println("Invert Decorator: " + notHealthLow.getDescription());

        // Repeat decorator example
        AttackNode attack = new AttackNode(null);
        RepeatDecorator repeatAttack = new RepeatDecorator(attack, 3);
        System.out.println("Repeat Decorator: " + repeatAttack.getDescription());

        // Timeout decorator example
        BehaviorTree fleeAction = new BehaviorTree() {
            @Override
            public ExecutionResult execute(IFakePlayer fakePlayer) {
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
        };
        fleeAction.setName("Flee");

        TimeoutDecorator timeoutFlee = new TimeoutDecorator(fleeAction, 5000);
        System.out.println("Timeout Decorator: " + timeoutFlee.getDescription());

        // Succeed decorator example
        BehaviorTree cleanupAction = new BehaviorTree() {
            @Override
            public ExecutionResult execute(IFakePlayer fakePlayer) {
                return failure(); // Even if this fails
            }

            @Override
            public void reset() {
                markNotRunning();
            }

            @Override
            public String getDescription() {
                return "Cleanup";
            }
        };
        cleanupAction.setName("Cleanup");

        SucceedDecorator alwaysSucceed = new SucceedDecorator(cleanupAction);
        System.out.println("Succeed Decorator: " + alwaysSucceed.getDescription());
        System.out.println();
    }

    private void demonstrateParallelExecution() {
        System.out.println("3. PARALLEL EXECUTION");
        System.out.println("=====================\n");

        IBehaviorTree parallelTree = factory.createParallelCombatTree();
        System.out.println("Parallel Combat Tree:");
        System.out.println(parallelTree.getDescription());
        System.out.println();

        // Show different parallel policies
        BehaviorTree action1 = createDemoAction("Action1");
        BehaviorTree action2 = createDemoAction("Action2");
        BehaviorTree action3 = createDemoAction("Action3");

        ParallelNode requireOne = new ParallelNode(
            new IBehaviorTree[]{action1, action2, action3},
            ParallelNode.Policy.REQUIRE_ONE,
            ParallelNode.Policy.REQUIRE_ONE
        );
        requireOne.setName("RequireOne");
        System.out.println("Parallel (Require One): " + requireOne.getDescription());

        ParallelNode requireAll = new ParallelNode(
            new IBehaviorTree[]{action1, action2, action3},
            ParallelNode.Policy.REQUIRE_ALL,
            ParallelNode.Policy.REQUIRE_ALL
        );
        requireAll.setName("RequireAll");
        System.out.println("Parallel (Require All): " + requireAll.getDescription());
        System.out.println();
    }

    private void demonstrateVisualization() {
        System.out.println("4. TREE VISUALIZATION");
        System.out.println("=====================\n");

        IBehaviorTree tree = factory.createAdvancedCombatTree();

        System.out.println("Tree Structure:");
        System.out.println(BehaviorTreeVisualizer.visualize(tree));
        System.out.println();

        System.out.println("Compact View:");
        System.out.println(BehaviorTreeVisualizer.visualizeCompact(tree));
        System.out.println();

        System.out.println("Node Count by Category:");
        BehaviorTreeVisualizer.countNodesByCategory(tree).forEach((category, count) ->
            System.out.println("  " + category + ": " + count));
        System.out.println();

        System.out.println("Full Report:");
        System.out.println(BehaviorTreeVisualizer.generateReport(tree));
    }

    private void demonstrateAdvancedBuilding() {
        System.out.println("5. ADVANCED TREE BUILDING");
        System.out.println("=========================\n");

        // Builder pattern
        IBehaviorTree customTree = factory.createCustomTree("MyCustomTree");
        System.out.println("Custom Tree (Builder Pattern):");
        System.out.println(BehaviorTreeVisualizer.visualizeCompact(customTree));
        System.out.println();

        // Dynamic priority tree
        IBehaviorTree dynamicTree = factory.createDynamicPriorityTree();
        System.out.println("Dynamic Priority Tree:");
        System.out.println(BehaviorTreeVisualizer.visualizeCompact(dynamicTree));
        System.out.println();

        // Complex nested tree
        IBehaviorTree complexTree = createComplexNestedTree();
        System.out.println("Complex Nested Tree:");
        System.out.println(BehaviorTreeVisualizer.visualize(complexTree));
    }

    private IBehaviorTree createComplexNestedTree() {
        // Create a complex tree with multiple layers
        BehaviorTree action1 = createDemoAction("Action1");
        BehaviorTree action2 = createDemoAction("Action2");
        BehaviorTree action3 = createDemoAction("Action3");

        // Layer 1: Parallel actions
        ParallelNode parallelLayer = new ParallelNode(
            new IBehaviorTree[]{action1, action2},
            ParallelNode.Policy.REQUIRE_ONE,
            ParallelNode.Policy.REQUIRE_ONE
        );
        parallelLayer.setName("ParallelLayer");

        // Layer 2: Sequence with timeout
        SequenceNode sequenceLayer = new SequenceNode(
            new HasTargetCondition(),
            new TimeoutDecorator(parallelLayer, 10000),
            action3
        );
        sequenceLayer.setName("SequenceLayer");

        // Layer 3: Selector with retry
        SelectorNode selectorLayer = new SelectorNode(
            sequenceLayer,
            new RepeatDecorator(createDemoAction("RetryAction"), 3)
        );
        selectorLayer.setName("SelectorLayer");

        return selectorLayer;
    }

    private BehaviorTree createDemoAction(String name) {
        BehaviorTree action = new BehaviorTree() {
            @Override
            public ExecutionResult execute(IFakePlayer fakePlayer) {
                // Demo action - just succeed
                return success();
            }

            @Override
            public void reset() {
                markNotRunning();
            }

            @Override
            public String getDescription() {
                return name;
            }

            @Override
            public String getCategory() {
                return "DemoAction";
            }
        };
        action.setName(name);
        return action;
    }

    /**
     * Show before/after comparison
     */
    public void showUpgradeComparison() {
        System.out.println("=== UPGRADE COMPARISON ===");
        System.out.println();

        System.out.println("BEFORE (Legacy System):");
        System.out.println("- Separate Status and BehaviorResult enums");
        System.out.println("- Basic composite nodes only");
        System.out.println("- No decorator nodes");
        System.out.println("- No parallel execution");
        System.out.println("- No tree visualization");
        System.out.println("- Hard to debug complex trees");
        System.out.println("- Limited reusability");
        System.out.println();

        System.out.println("AFTER (Upgraded System):");
        System.out.println("✓ Unified ExecutionResult enum");
        System.out.println("✓ Modern decorator nodes (Invert, Repeat, Succeed, Timeout)");
        System.out.println("✓ Parallel execution with configurable policies");
        System.out.println("✓ Tree visualization and debugging tools");
        System.out.println("✓ Builder pattern for complex trees");
        System.out.println("✓ Better type safety and error handling");
        System.out.println("✓ Enhanced performance and maintainability");
        System.out.println("✓ Comprehensive tree introspection");
        System.out.println();

        System.out.println("NEW FEATURES DEMONSTRATION:");
        runFullDemo();
    }

    /**
     * Performance comparison (simplified)
     */
    public void performanceDemo() {
        System.out.println("=== PERFORMANCE DEMO ===");
        System.out.println();

        // Create a large tree for performance testing
        IBehaviorTree largeTree = createLargeTree(100);
        System.out.println("Created large tree with " +
            BehaviorTreeVisualizer.countNodesByCategory(largeTree).values().stream()
                .mapToInt(Integer::intValue).sum() + " nodes");

        // Test visualization performance
        long startTime = System.nanoTime();
        String visualization = BehaviorTreeVisualizer.visualize(largeTree);
        long endTime = System.nanoTime();
        System.out.println("Visualization took: " + (endTime - startTime) / 1_000_000 + "ms");
        System.out.println("Visualization length: " + visualization.length() + " characters");
    }

    private IBehaviorTree createLargeTree(int depth) {
        if (depth <= 0) {
            return createDemoAction("Leaf" + depth);
        }

        return new SelectorNode(
            new SequenceNode(
                new HasTargetCondition(),
                createLargeTree(depth - 1)
            ),
            createLargeTree(depth - 1)
        );
    }
}
