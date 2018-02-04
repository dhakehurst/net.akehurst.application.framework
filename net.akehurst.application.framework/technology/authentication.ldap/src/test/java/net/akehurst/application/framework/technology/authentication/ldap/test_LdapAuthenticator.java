package net.akehurst.application.framework.technology.authentication.ldap;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.common.interfaceUser.UserDetails;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.common.test.AbstractTestCase;
import net.akehurst.application.framework.common.test.TestConfigurationService;
import net.akehurst.application.framework.common.test.TestFramework;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorRequest;

public class test_LdapAuthenticator extends AbstractTestCase {

    testContext_LdapAuthenticator testContext;

    @Ignore
    @Test
    public void test1() {
        try {
            final TestFramework tf = new TestFramework("af", "af");
            final TestConfigurationService configuration = tf.createServiceInstance("configuration", TestConfigurationService.class, "test.configuration");

            configuration.set("sut.handler.url", "ldaps://filer.itemis.de:636");
            configuration.set("sut.handler.adminNamePattern", "");
            configuration.set("sut.handler.adminPassword", "");
            configuration.set("sut.handler.userSearchRoot", "dc=itemis,dc=de");
            configuration.set("sut.handler.userSecurityLevel", "simple");

            final Map<String, IService> services = new HashMap<>();
            services.put("configuration", configuration);
            this.testContext = tf.createTestEnvironment(testContext_LdapAuthenticator.class, services, new String[0]);
            this.testContext.afStart();

            final UserDetails user = new UserDetails("<unknown>");
            final UserSession session = new UserSession("test-session", user, null);

            super.perform(this.testContext.tc1.handler.authenticatorRequest, IAuthenticatorRequest::requestLogin, session, "username", "password");
            super.delay(200);
            super.expect(this.testContext.tc1.handler, IAuthenticatorNotification::notifyAuthenticationSuccess, session);

            super.play();
            super.sleep(1000);
            super.verify();

        } catch (final Exception e) {
            Assert.fail(e.getMessage());
        }
    }

}
