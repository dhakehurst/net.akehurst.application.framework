package net.akehurst.application.framework.technology.interfaceGui.data.graph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.annotations.declaration.DataType;

@DataType
public class GuiGraphNode extends AbstractDataType implements IGuiGraphNode {

	public GuiGraphNode(final String identity, final IGuiGraphNode parent, final String... classes) {
		super(identity);
		this.parent = parent;
		this.classes = Arrays.asList(classes);
		this.data = new HashMap<>();
		// add to data do they can be used in the styling
		this.data.put("identity", identity);
	}

	private final IGuiGraphNode parent;
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
	public List<String> getClasses() {
		return this.classes;
	}

	@Override
	public Map<String, Object> getData() {
		return this.data;
	}

}
