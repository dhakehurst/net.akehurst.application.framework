package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.annotations.declaration.DataType;

@DataType
public class GuiGraphEdge extends AbstractDataType implements IGuiGraphEdge {

	public GuiGraphEdge(final String identity, final IGuiGraphNode parent, final IGuiGraphNode source, final IGuiGraphNode target, final String... classes) {
		super(identity);
		this.parent = parent;
		this.source = source;
		this.target = target;

		this.classes = classes;
		this.data = new HashMap<>();
		// add to data do they can be used in the styling
		this.data.put("identity", identity);
	}

	private final IGuiGraphNode parent;
	private final IGuiGraphNode source;
	private final IGuiGraphNode target;

	private final String[] classes;
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
		return this.source;
	}

	@Override
	public IGuiGraphNode getTarget() {
		return this.target;
	}

	@Override
	public String[] getClasses() {
		return this.classes;
	}

	@Override
	public Map<String, Object> getData() {
		return this.data;
	}

}
