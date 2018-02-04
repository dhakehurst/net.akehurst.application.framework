package net.akehurst.application.framework.technology.authentication.ldap;

import net.akehurst.application.framework.realisation.AbstractIdentifiableObject;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorRequest;

abstract public class testHandler extends AbstractIdentifiableObject implements IAuthenticatorNotification {

    public testHandler(final String afId) {
        super(afId);
    }

    public IAuthenticatorRequest authenticatorRequest;

}
