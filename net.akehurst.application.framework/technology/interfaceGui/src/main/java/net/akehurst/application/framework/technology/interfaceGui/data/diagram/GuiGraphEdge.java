package net.akehurst.application.framework.technology.interfaceGui.data.diagram;

import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.annotations.declaration.DataType;

@DataType
public class GuiGraphEdge extends AbstractDataType implements IGuiGraphEdge {

	public GuiGraphEdge(final IGuiGraphNode source, final IGuiGraphNode target, final String label, final String type) {
		super(source, target, label);
		this.type = type;
		this.data = new HashMap<>();
	}

	@Override
	public IGuiGraphNode getSource() {
		return (IGuiGraphNode) super.getIdentityValues().get(0);
	}

	@Override
	public IGuiGraphNode getTarget() {
		return (IGuiGraphNode) super.getIdentityValues().get(1);
	}

	@Override
	public String getLabel() {
		return (String) super.getIdentityValues().get(2);
	}

	String type;

	public String getType() {
		return this.type;
	}

	Map<String, String> data;

	@Override
	public Map<String, String> getData() {
		return this.data;
	}

}
