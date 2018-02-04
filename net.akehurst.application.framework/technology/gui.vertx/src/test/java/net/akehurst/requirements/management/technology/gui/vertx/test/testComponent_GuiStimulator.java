package net.akehurst.requirements.management.technology.gui.vertx.test;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.test.annotation.MockPassiveObjectInstance;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.interfaceGui.IGuiNotification;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;

public class testComponent_GuiStimulator extends AbstractComponent {

    public testComponent_GuiStimulator(final String afId) {
        super(afId);
    }

    @MockPassiveObjectInstance
    testObject_Handler handler;

    @Override
    public void afConnectParts() {
        this.portGui().connectInternal(this.handler);
    }

    @PortInstance
    @PortContract(provides = IGuiNotification.class, requires = IGuiRequest.class)
    private IPort portGui;

    public IPort portGui() {
        return this.portGui;
    }

}