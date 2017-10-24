package net.akehurst.application.framework.technology.interfaceGui.data.graph;

import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.annotations.declaration.DataType;

@DataType
public class GuiGraphNode extends AbstractDataType implements IGuiGraphNode {

	public GuiGraphNode(final String identity, final IGuiGraphNode parent) {
		super(identity);
		this.parent = parent;
		this.data = new HashMap<>();
		this.properties = new HashMap<>();
		// add to data so they can be used in the styling
		this.data.put("identity", identity);
	}

	private final IGuiGraphNode parent;
	private final Map<String, Object> data;
	private final Map<String, Object> properties;

	@Override
	public String getIdentity() {
		return (String) super.getIdentityValues().get(0);
	}

	@Override
	public IGuiGraphNode getParent() {
		return this.parent;
	}

	@Override
	public Map<String, Object> getProperties() {
		return this.properties;
	}

	@Override
	public Map<String, Object> getData() {
		return this.data;
	}

}
