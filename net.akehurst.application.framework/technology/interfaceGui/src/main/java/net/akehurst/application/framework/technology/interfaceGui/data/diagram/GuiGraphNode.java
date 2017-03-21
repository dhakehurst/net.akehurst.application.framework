package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.annotations.declaration.DataType;

@DataType
public class GuiGraphNode extends AbstractDataType implements IGuiGraphNode {

	public GuiGraphNode(final String identity, final IGuiGraphNode parent, final String... types) {
		super(identity);
		this.parent = parent;
		this.types = types;
		this.data = new HashMap<>();
		// add to data do they can be used in the styling
		this.data.put("identity", identity);
	}

	private final IGuiGraphNode parent;
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
	public String[] getType() {
		return this.types;
	}

	@Override
	public Map<String, String> getData() {
		return this.data;
	}

}
