package net.akehurst.requirements.management.technology.gui.vertx.test;

import net.akehurst.application.framework.realisation.IdentifiableObjectAbstract;
import net.akehurst.application.framework.technology.interfaceGui.IGuiNotification;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;

public abstract class testObject_Handler extends IdentifiableObjectAbstract implements IGuiNotification {

    public testObject_Handler(final String afId) {
        super(afId);
    }

    public IGuiRequest guiRequest;

}
