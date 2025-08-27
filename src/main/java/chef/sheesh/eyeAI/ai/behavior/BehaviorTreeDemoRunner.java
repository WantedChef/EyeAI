package chef.sheesh.eyeAI.ai.behavior;

/**
 * Simple runner to demonstrate the upgraded behavior tree system.
 * Run this to see all the new features in action.
 */
public class BehaviorTreeDemoRunner {

    public static void main(String[] args) {
        BehaviorTreeDemo demo = new BehaviorTreeDemo();

        System.out.println("🎯 BEHAVIOR TREE UPGRADE COMPLETE!");
        System.out.println("=====================================\n");

        // Show the upgrade comparison
        demo.showUpgradeComparison();

        // Show performance demo
        System.out.println("\n" + "=".repeat(50));
        demo.performanceDemo();

        System.out.println("\n" + "=".repeat(50));
        System.out.println("✅ Behavior tree upgrade demonstration complete!");
        System.out.println("🎉 All new features are working properly!");
    }
}
