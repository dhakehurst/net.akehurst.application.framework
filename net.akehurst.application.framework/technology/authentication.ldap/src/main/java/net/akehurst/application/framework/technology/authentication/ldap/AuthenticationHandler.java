package net.akehurst.application.framework.technology.authentication.ldap;

import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.xml.bind.DatatypeConverter;

import net.akehurst.application.framework.common.annotations.declaration.ExternalConnection;
import net.akehurst.application.framework.common.annotations.instance.CommandLineArgument;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.common.interfaceUser.UserDetails;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.realisation.AbstractActiveSignalProcessingObject;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorRequest;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class AuthenticationHandler extends AbstractActiveSignalProcessingObject implements IAuthenticatorRequest {

	@ServiceReference
	ILogger logger;

	public AuthenticationHandler(final String afId) {
		super(afId);
	}

	@ExternalConnection
	public IAuthenticatorNotification authenticationNotification;

	@ConfiguredValue(defaultValue = "ldap://...")
	private String url;

	@ConfiguredValue(defaultValue = "none")
	private String adminSecurityLevel;

	@ConfiguredValue(defaultValue = "cn=%s,dc=company,dc=org")
	private String adminUserPattern;

	@ConfiguredValue(defaultValue = "")
	private String adminUser;

	@CommandLineArgument(description = "Password for LDAP admin")
	@ConfiguredValue(defaultValue = "")
	private String adminPassword;

	@ConfiguredValue(defaultValue = "")
	private List<String> userAttributes;

	@ConfiguredValue(defaultValue = "dn=org")
	private String userSearchRoot;

	@ConfiguredValue(defaultValue = "(uid=%s)")
	private String userSearchPattern;

	@ConfiguredValue(defaultValue = "none")
	private String userSecurityLevel;

	@ConfiguredValue(defaultValue = "cn=%s,dc=company,dc=org")
	private String userNamePattern;

	@Override
	public void requestLogin(final UserSession session, final String username, final String password) {
		super.submit("requestLogin", () -> {

			try {
				// authenticate admin
				final Hashtable<String, String> adminProps = new Hashtable<>();
				adminProps.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				adminProps.put(Context.PROVIDER_URL, this.url);
				adminProps.put(Context.SECURITY_AUTHENTICATION, this.adminSecurityLevel);
				final String principalName = String.format(this.adminUserPattern, this.adminUser);
				adminProps.put(Context.SECURITY_PRINCIPAL, principalName);
				adminProps.put(Context.SECURITY_CREDENTIALS, this.adminPassword);

				final DirContext authContext = new InitialDirContext(adminProps);

				// find user
				final SearchControls search = new SearchControls();
				search.setReturningAttributes(new String[] {});
				search.setSearchScope(SearchControls.SUBTREE_SCOPE);

				final String userSearch = String.format(this.userSearchPattern, username);
				final NamingEnumeration<SearchResult> results = authContext.search(this.userSearchRoot, userSearch, search);
				if (results.hasMoreElements()) {
					final SearchResult result = results.nextElement();
					final String user = result.getNameInNamespace();

					// authenticate user
					final Hashtable<String, String> userProps = new Hashtable<>();
					userProps.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
					userProps.put(Context.PROVIDER_URL, this.url);
					userProps.put(Context.SECURITY_AUTHENTICATION, this.userSecurityLevel);
					userProps.put(Context.SECURITY_PRINCIPAL, user);
					final String decPswd = this.decrypt(password);
					userProps.put(Context.SECURITY_CREDENTIALS, decPswd);

					final DirContext userContext = new InitialDirContext(userProps);

					final UserSession authenticatedSession = new UserSession(session.getId(), new UserDetails(username));

					this.authenticationNotification.notifyAuthenticationSuccess(authenticatedSession);
				} else {
					this.authenticationNotification.notifyAuthenticationFailure(session, "Cannot find " + userSearch);
				}
			} catch (final NamingException e) {
				this.logger.log(LogLevel.DEBUG, e.getMessage(), e);
				this.authenticationNotification.notifyAuthenticationFailure(session, e.getMessage());
			}

		});
	}

	static final String V = "1234567890abcdef1234567890abcdef";

	private String decrypt(final String encrypted) {
		try {
			final byte[] bytes = DatatypeConverter.parseHexBinary(AuthenticationHandler.V);
			final SecretKeySpec key = this.createKey(AuthenticationHandler.V, bytes);
			final IvParameterSpec iv = new IvParameterSpec(bytes);
			final String decrypted = this.decrypt(encrypted, key, iv);
			return decrypted;
		} catch (final GeneralSecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	SecretKeySpec createKey(final String password, final byte[] saltBytes) throws GeneralSecurityException {

		final KeySpec keySpec = new PBEKeySpec(password.toCharArray(), saltBytes, 100, 128);
		final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		final SecretKey secretKey = keyFactory.generateSecret(keySpec);
		return new SecretKeySpec(secretKey.getEncoded(), "AES");
	}

	private String decrypt(final String encrypted, final SecretKeySpec key, final IvParameterSpec iv) throws GeneralSecurityException {

		final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		final byte[] decodedValue = Base64.getDecoder().decode(encrypted);
		final byte[] decValue = cipher.doFinal(decodedValue);
		final String decryptedValue = new String(decValue);
		return decryptedValue;
	}

	@Override
	public void requestLogout(final UserSession session) {
		super.submit("requestLogout", () -> {
			final UserSession clearedSession = new UserSession(session.getId(), null);
			this.authenticationNotification.notifyAuthenticationCleared(clearedSession);
		});
	}

}
