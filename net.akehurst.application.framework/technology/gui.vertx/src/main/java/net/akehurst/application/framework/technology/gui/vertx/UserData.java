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
package net.akehurst.application.framework.technology.gui.vertx;

import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.impl.UserHolder;
import net.akehurst.application.framework.common.DataTypeAbstract;
import net.akehurst.application.framework.common.IUser;

public class UserData extends DataTypeAbstract implements IUser {

	public UserData(final Session session) {
		this.session = session;
	}

	Session session;

	public UserData(final String name) {
		super(name);
		this.name = name;
	}

	String name;

	@Override
	public String getName() {
		if (null == this.session) {
			return this.name;
		} else {
			final UserHolder holder = this.session.get("__vertx.userHolder");
			return null == holder.user ? null : holder.user.principal().getString("username");
		}
	}

}
