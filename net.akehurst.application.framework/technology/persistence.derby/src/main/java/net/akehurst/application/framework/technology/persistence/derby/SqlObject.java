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
package net.akehurst.application.framework.technology.persistence.derby;

import java.lang.reflect.Method;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Map;

public class SqlObject<T> implements SQLData {

	/**
	 * for the db driver to call
	 */
	public SqlObject() {
	}
	
	public SqlObject(Class<T> type, T object) {
		this.type = type;
		this.object = object;
	}
	
	Class<T> type;
	T object;
	
	@Override
	public String getSQLTypeName() throws SQLException {
		return this.type.getSimpleName();
	}

	@Override
	public void readSQL(SQLInput stream, String typeName) throws SQLException {

	}

	@Override
	public void writeSQL(SQLOutput stream) throws SQLException {
		try {
			for(Method m:this.type.getMethods()) {
				if (m.getName().startsWith("get")) {
					String key = m.getName().substring(3);
					Object value = m.invoke(this.object);
					this.writeObject(stream, value);
				} else {
					
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void writeObject(SQLOutput stream, Object value) throws SQLException {
		if (value instanceof SQLData) {
			stream.writeObject((SQLData)value);
		} else if (value instanceof String) {
			stream.writeString((String)value);
		}
	}

}
