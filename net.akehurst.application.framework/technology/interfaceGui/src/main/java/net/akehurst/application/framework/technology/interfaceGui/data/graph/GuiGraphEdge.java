package net.akehurst.application.framework.technology.interfaceGui.data.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.annotations.declaration.DataType;

@DataType
public class GuiGraphEdge extends AbstractDataType implements IGuiGraphEdge {

    public GuiGraphEdge(final IGuiGraph graph, final String identity, final IGuiGraphNode parent, final String sourceNodeId, final String targetNodeId,
            final String... classes) {
        super(identity);
        this.graph = graph;
        this.parent = parent;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;

        this.classes = new ArrayList<>(Arrays.asList(classes));
        this.data = new HashMap<>();
        // add to data do they can be used in the styling
        this.data.put("identity", identity);
    }

    private final IGuiGraph graph;
    private final IGuiGraphNode parent;
    private final String sourceNodeId;
    private final String targetNodeId;

    private final List<String> classes;
    private final Map<String, Object> data;

    @Override
    public String getIdentity() {
        return (String) super.getIdentityValues().get(0);
    }

    @Override
    public IGuiGraphNode getParent() {
        return this.parent;
    }

    @Override
    public IGuiGraphNode getSource() {
        final IGuiGraphNode source = this.graph.getNodes().get(this.sourceNodeId);
        return source;
    }

    @Override
    public IGuiGraphNode getTarget() {
        final IGuiGraphNode target = this.graph.getNodes().get(this.targetNodeId);
        return target;
    }

    @Override
    public List<String> getClasses() {
        return this.classes;
    }

    @Override
    public Map<String, Object> getData() {
        return this.data;
    }

}
