package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modern abstract base class for composite nodes that contain child nodes.
 * Composite nodes control the execution flow of their children.
 */
public abstract class CompositeNode extends BehaviorTree {

    protected final List<IBehaviorTree> children = new ArrayList<>();
    protected int currentChildIndex = 0;

    public CompositeNode() {
        super();
    }

    public CompositeNode(IBehaviorTree... children) {
        for (IBehaviorTree child : children) {
            addChild(child);
        }
    }

    /**
     * Add a child node to this composite
     */
    public void addChild(IBehaviorTree child) {
        if (child != null) {
            children.add(child);
        }
    }

    /**
     * Add multiple children at once
     */
    public void addChildren(IBehaviorTree... children) {
        for (IBehaviorTree child : children) {
            addChild(child);
        }
    }

    /**
     * Remove a child node
     */
    public boolean removeChild(IBehaviorTree child) {
        boolean removed = children.remove(child);
        if (removed && currentChildIndex >= children.size()) {
            currentChildIndex = Math.max(0, children.size() - 1);
        }
        return removed;
    }

    /**
     * Remove child at specific index
     */
    public IBehaviorTree removeChild(int index) {
        if (index >= 0 && index < children.size()) {
            IBehaviorTree removed = children.remove(index);
            if (currentChildIndex >= children.size()) {
                currentChildIndex = Math.max(0, children.size() - 1);
            }
            return removed;
        }
        return null;
    }

    /**
     * Get all children (unmodifiable view)
     */
    public List<IBehaviorTree> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Check if this composite has any children
     */
    public boolean hasChildren() {
        return children.isEmpty();
    }

    /**
     * Get the number of children
     */
    public int getChildCount() {
        return children.size();
    }

    /**
     * Get child at specific index
     */
    public IBehaviorTree getChild(int index) {
        if (index >= 0 && index < children.size()) {
            return children.get(index);
        }
        return null;
    }

    @Override
    public void reset() {
        currentChildIndex = 0;
        for (IBehaviorTree child : children) {
            child.reset();
        }
        markNotRunning();
    }

    /**
     * Get the current child being executed
     */
    protected IBehaviorTree getCurrentChild() {
        if (currentChildIndex < children.size()) {
            return children.get(currentChildIndex);
        }
        return null;
    }

    /**
     * Move to the next child
     */
    protected boolean nextChild() {
        if (currentChildIndex < children.size() - 1) {
            currentChildIndex++;
            return false;
        }
        return true;
    }

    /**
     * Reset to first child
     */
    protected void resetToFirstChild() {
        currentChildIndex = 0;
    }

    /**
     * Set current child index
     */
    protected void setCurrentChildIndex(int index) {
        if (index >= 0 && index < children.size()) {
            currentChildIndex = index;
        }
    }

    @Override
    public String getCategory() {
        return "Composite";
    }

    @Override
    public String getDescription() {
        return String.format("%s (%d children)", getClass().getSimpleName(), children.size());
    }

    /**
     * Abstract execute method to be implemented by subclasses
     */
    @Override
    public abstract ExecutionResult execute(IFakePlayer fakePlayer);
}
