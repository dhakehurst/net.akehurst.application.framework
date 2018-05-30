package net.akehurst.requirements.management.technology.gui.vertx.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.common.test.TestConfigurationService;
import net.akehurst.application.framework.common.test.TestFramework;
import net.akehurst.application.framework.technology.interfaceGui.GuiException;
import net.akehurst.application.framework.technology.interfaceGui.IGuiRequest;
import net.akehurst.application.framework.technology.interfaceGui.SceneIdentity;
import net.akehurst.application.framework.technology.interfaceGui.StageIdentity;

public class test_oauthAuthorise {

    testContext_VertxWebsite testContext;

    @Ignore
    @Test
    public void test() throws ApplicationFrameworkException, GuiException {
        final TestFramework tf = new TestFramework("af", "af");
        final TestConfigurationService configuration = tf.createServiceInstance("configuration", TestConfigurationService.class, "test.configuration");
        configuration.set("sut.libClassPath", "");

        final Map<String, IService> services = new HashMap<>();
        services.put("configuration", configuration);
        this.testContext = tf.createTestEnvironment(testContext_VertxWebsite.class, services, new String[0]);
        this.testContext.afStart();

        final UserSession session = new UserSession("testId", null, null);
        final String clientId = "425940381741-pucip8u310q19abnnp3t8lfurjs5tse6.apps.googleusercontent.com";
        final String clientSecret = "_1256nHEyTKiumHCqvKg1m62";
        final String site = "https://accounts.google.com";
        final String tokenPath = "https://www.googleapis.com/oauth2/v3/token";
        final String authorisationPath = "/o/oauth2/auth";
        final String scopes = "";

        final StageIdentity stageId = new StageIdentity("xxx");
        final SceneIdentity sceneId = new SceneIdentity("home");
        this.testContext.sut.portGui().in(IGuiRequest.class).createStage(stageId, "/af", null, null, true);
        this.testContext.sut.portGui().in(IGuiRequest.class).createScene(stageId, sceneId, ITestScene.class, null);

        this.testContext.sut.portGui().in(IGuiRequest.class).oauthAuthorise(session, clientId, clientSecret, site, tokenPath, authorisationPath, scopes);

        Assert.assertTrue(true);
    }

}
