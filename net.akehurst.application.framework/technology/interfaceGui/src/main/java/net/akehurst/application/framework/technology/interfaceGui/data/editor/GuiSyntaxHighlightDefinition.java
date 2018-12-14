package net.akehurst.application.framework.technology.interfaceGui.data.editor;

import net.akehurst.application.framework.common.annotations.datatype.DataType;
import net.akehurst.datatype.common.DatatypeAbstract;

@DataType
public class GuiSyntaxHighlightDefinition extends DatatypeAbstract implements IGuiSyntaxHighlightDefinition {

    public GuiSyntaxHighlightDefinition(final String pattern, final String label) {
        super(pattern, label);
    }

    @Override
    public String getPattern() {
        return (String) super.getIdentityValues().get(0);
    }

    @Override
    public String getLable() {
        return (String) super.getIdentityValues().get(1);
    }

}
