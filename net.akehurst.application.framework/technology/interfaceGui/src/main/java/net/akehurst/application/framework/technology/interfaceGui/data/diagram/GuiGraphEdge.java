package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.annotations.declaration.DataType;

@DataType
public class GuiGraphEdge extends AbstractDataType implements IGuiGraphEdge {

	public GuiGraphEdge(final String identity, final IGuiGraphNode parent, final IGuiGraphNode source, final IGuiGraphNode target, final String... types) {
		super(identity);
		this.parent = parent;
		this.source = source;
		this.target = target;

		this.types = types;
		this.data = new HashMap<>();
		// add to data do they can be used in the styling
		this.data.put("identity", identity);
	}

	private final IGuiGraphNode parent;
	private final IGuiGraphNode source;
	private final IGuiGraphNode target;

	private final String[] types;
	private final Map<String, String> data;

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
	public String[] getType() {
		return this.types;
	}

	@Override
	public Map<String, String> getData() {
		return this.data;
	}

}
