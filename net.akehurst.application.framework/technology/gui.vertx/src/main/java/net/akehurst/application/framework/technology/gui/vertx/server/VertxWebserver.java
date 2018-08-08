package net.akehurst.application.framework.technology.gui.vertx.server;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.declaration.Component;
import net.akehurst.application.framework.common.annotations.instance.ActiveObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.realisation.ComponentAbstract;
import net.akehurst.application.framework.technology.gui.api.GuiTheaterNotification;
import net.akehurst.application.framework.technology.gui.api.GuiTheaterRequest;

@Component
public class VertxWebserver extends ComponentAbstract {

    @ActiveObjectInstance
    GuiTheaterHandler handler;

    public VertxWebserver(final String afId) {
        super(afId);
    }

    @Override
    public void afConnectParts() {
        this.portGui().connectInternal(this.handler);
    }

    // --------- Ports ---------
    @PortInstance
    @PortContract(provides = GuiTheaterRequest.class, requires = GuiTheaterNotification.class)
    // @PortContract(provides = IAuthenticatorRequest.class, requires = IAuthenticatorNotification.class)
    IPort portGui;

    public IPort portGui() {
        return this.portGui;
    }

}
