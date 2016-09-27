package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.annotations.declaration.DataType;

@DataType
public class GuiGraphNode extends AbstractDataType implements IGuiGraphNode {

	public GuiGraphNode(final IGuiGraphNode parent, final String identity, final String... types) {
		super(identity);
		this.types = types;
		this.data = new HashMap<>();
	}

	@Override
	public String getIdentity() {
		return (String) super.getIdentityValues().get(0);
	}

	IGuiGraphNode parent;

	public IGuiGraphNode getParent() {
		return this.parent;
	}

	String[] types;

	@Override
	public String[] getType() {
		return this.types;
	}

	Map<String, String> data;

	@Override
	public Map<String, String> getData() {
		return this.data;
	}

}
