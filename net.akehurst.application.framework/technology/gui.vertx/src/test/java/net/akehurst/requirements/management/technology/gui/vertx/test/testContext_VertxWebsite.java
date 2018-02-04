package net.akehurst.requirements.management.technology.gui.vertx.test;

import net.akehurst.application.framework.common.annotations.instance.ComponentInstance;
import net.akehurst.application.framework.common.test.AbstractTestContext;
import net.akehurst.application.framework.common.test.annotation.TestComponentInstance;
import net.akehurst.application.framework.common.test.annotation.TestContext;
import net.akehurst.application.framework.technology.gui.vertx.VertxWebsite;

@TestContext
public class testContext_VertxWebsite extends AbstractTestContext {

    @ComponentInstance
    public VertxWebsite sut;

    @TestComponentInstance
    public testComponent_GuiStimulator stimulator;

    @Override
    public void afConnectParts() {
        this.sut.portGui().connect(this.stimulator.portGui());
    }

}
