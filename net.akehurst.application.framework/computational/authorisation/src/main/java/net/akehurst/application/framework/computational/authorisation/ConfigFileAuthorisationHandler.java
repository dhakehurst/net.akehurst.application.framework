/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.application.framework.computational.authorisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.declaration.Component;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.common.interfaceUser.UserDetails;
import net.akehurst.application.framework.computational.interfaceAccessControl.AuthorisationActivity;
import net.akehurst.application.framework.computational.interfaceAccessControl.AuthorisationException;
import net.akehurst.application.framework.computational.interfaceAccessControl.AuthorisationSubject;
import net.akehurst.application.framework.computational.interfaceAccessControl.AuthorisationTarget;
import net.akehurst.application.framework.computational.interfaceAccessControl.IAuthorisationRequest;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

@Component
public class ConfigFileAuthorisationHandler extends AbstractComponent implements IAuthorisationRequest {

    public ConfigFileAuthorisationHandler(final String afId) {
        super(afId);
    }

    @ServiceReference
    ILogger logger;

    @ConfiguredValue(service = "authorisation", defaultValue = "")
    List<String> activities;

    List<AuthorisationActivity> activities_cache;

    List<AuthorisationActivity> getActivities() {
        if (null == this.activities_cache) {
            if (this.activities.isEmpty()) {
                this.logger.log(LogLevel.ERROR, "No Activities defined in authorisation config file");
                this.activities_cache = new ArrayList<>();
            } else {
                this.activities_cache = this.activities.stream().map(el -> new AuthorisationActivity(el)).collect(Collectors.toList());

            }
        }
        return this.activities_cache;
    }

    @ConfiguredValue(service = "authorisation", defaultValue = "")
    List<Map<String, String>> permissions;
    List<AuthorisationPermission> permissions_cache;

    List<AuthorisationPermission> getPermissions() {
        if (null == this.permissions_cache) {
            this.permissions_cache = this.permissions.stream().map(el -> {
                final AuthorisationSubject subject = new AuthorisationSubject(el.get("subject"));
                final AuthorisationActivity activity = new AuthorisationActivity(el.get("activity"));
                final AuthorisationTarget target = new AuthorisationTarget(el.get("target"));
                return new AuthorisationPermission(subject, activity, target);
            }).collect(Collectors.toList());
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
