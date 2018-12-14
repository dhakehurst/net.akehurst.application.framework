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
package net.akehurst.application.framework.common.interfaceUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.akehurst.application.framework.common.Context;
import net.akehurst.datatype.common.DatatypeAbstract;

public class UserSession extends DatatypeAbstract {

    /**
     *
     * @param sessionId
     * @param user
     * @param data
     *            entries are copies into this UserSessions data
     */
    public UserSession(final String sessionId, final UserDetails user, final Map<String, Object> data) {
        this.sessionId = sessionId;
        this.user = user;
        this.data = null == data ? new HashMap<>() : new HashMap<>(data);
        this.contexts = new ArrayList<>();
    }

    private final String sessionId;
    private final UserDetails user;
    private final Map<String, Object> data;
    private final List<Context> contexts;

    public String getId() {
        return this.sessionId;
    }

    public UserDetails getUser() {
        return this.user;
    }

    public boolean isAuthenticated() {
        return null != this.getUser();
    }

    public Map<String, Object> getData() {
        return this.data;
    }

    public Context getContext() {
        if (this.contexts.isEmpty()) {
            return null;
        } else {
            return this.contexts.get(this.contexts.size() - 1);
        }
    }

    public Context pushNewContext() {
        final Context c = new ContextDefault(this);
        this.contexts.add(c);
        return c;
    }

    public Context popContext() {
        if (this.contexts.isEmpty()) {
            return null;
        } else {
            final Context c = this.getContext();
            this.contexts.remove(this.contexts.size() - 1);
            return c;
        }
    }
}
