package net.akehurst.application.framework.service.authorisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.declaration.Component;
import net.akehurst.application.framework.common.annotations.instance.IdentifiableObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.common.interfaceUser.UserDetails;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.service.interfaceAccessControl.AuthorisationActivity;
import net.akehurst.application.framework.service.interfaceAccessControl.AuthorisationException;
import net.akehurst.application.framework.service.interfaceAccessControl.AuthorisationSubject;
import net.akehurst.application.framework.service.interfaceAccessControl.AuthorisationTarget;
import net.akehurst.application.framework.service.interfaceAccessControl.IAuthorisationRequest;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemQuery;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;
import net.akehurst.application.framework.technology.persistence.filesystem.HJsonFile;

@Component
public class ConfigFileAuthorisationHandler extends AbstractComponent implements IAuthorisationRequest {

	public ConfigFileAuthorisationHandler(final String afId) {
		super(afId);
	}

	@ServiceReference
	ILogger logger;

	@IdentifiableObjectInstance
	HJsonFile file;

	List<AuthorisationActivity> activities_cache;

	List<AuthorisationActivity> getActivities() {
		if (null == this.activities_cache) {
			try {
				final IPersistenceTransaction transaction = this.file.startTransaction();
				final PersistentItemQuery query = new PersistentItemQuery("activities");
				final List<String> activities = this.file.retrieve(transaction, query, List.class);
				if (null == activities) {
					this.logger.log(LogLevel.ERROR, "No Activities defined in " + this.file.getFile().getName());
					this.activities_cache = new ArrayList<>();
				} else {
					this.activities_cache = activities.stream().map(el -> new AuthorisationActivity(el)).collect(Collectors.toList());

				}
				this.file.commitTransaction(transaction);
			} catch (final PersistentStoreException e) {
				e.printStackTrace();
			}
		}
		return this.activities_cache;
	}

	List<AuthorisationPermission> permissions_cache;

	List<AuthorisationPermission> getPermissions() {
		if (null == this.permissions_cache) {
			try {
				final IPersistenceTransaction transaction = this.file.startTransaction();

				final PersistentItemQuery query = new PersistentItemQuery("permissions");
				final List<Map<String, String>> perms = this.file.retrieve(transaction, query, List.class);

				this.permissions_cache = perms.stream().map(el -> {
					final AuthorisationSubject subject = new AuthorisationSubject(el.get("subject"));
					final AuthorisationActivity activity = new AuthorisationActivity(el.get("activity"));
					final AuthorisationTarget target = new AuthorisationTarget(el.get("target"));
					return new AuthorisationPermission(subject, activity, target);
				}).collect(Collectors.toList());

				this.file.commitTransaction(transaction);
			} catch (final PersistentStoreException e) {
				e.printStackTrace();
			}
		}
		return this.permissions_cache;
	}

	void checkRegistered(final AuthorisationActivity activity) throws AuthorisationException {
		final boolean v = this.getActivities().contains(activity);
		if (v) {
			// all ok;
		} else {
			throw new AuthorisationException("Activity is not registered " + activity, null);
		}
	}

	boolean matches(final String definition, final String value) {
		final boolean v = definition.equals(value);
		if (v) {
			return true;
		} else {
			final boolean r = value.matches(definition);
			return r;
		}
	}

	boolean matches(final AuthorisationSubject subject, final UserDetails user) {
		final String subjectDefn = subject.getIdentity();
		final boolean b = this.matches(subjectDefn, user.getName());
		if (b) {
			return true;
		} else {
			for (final String grp : user.getGroups()) {
				if (this.matches(subjectDefn, grp)) {
					return true;
				}
			}
		}
		return false;
	}

	boolean check(final AuthorisationPermission p, final UserDetails user, final AuthorisationActivity activity, final AuthorisationTarget target) {
		final boolean subjectUserMatch = this.matches(p.getSubject(), user);
		final boolean activityMatch = this.matches(p.getActivity().getIdentity(), activity.getIdentity());
		final boolean targetMatch = this.matches(p.getTarget().getIdentity(), target.getIdentity());
		return subjectUserMatch && activityMatch && targetMatch;
	}

	@Override
	public boolean hasAuthorisation(final UserDetails user, final AuthorisationActivity activity, final AuthorisationTarget target)
			throws AuthorisationException {
		this.checkRegistered(activity);

		final Optional<AuthorisationPermission> perm = this.getPermissions().stream().filter((p) -> {
			return this.check(p, user, activity, target);
		}).findFirst();

		return perm.isPresent();
	}

	@Override
	public void grantAuthorisation(final AuthorisationSubject subject, final AuthorisationActivity permission, final AuthorisationTarget target) {
		throw new RuntimeException("Authorisations are granted by editing the file");
	}

	@PortInstance
	@PortContract(provides = IAuthorisationRequest.class)
	IPort portClient;

	public IPort portClient() {
		return this.portClient;
	}
}
