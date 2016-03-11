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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.akehurst.application.framework.components.AbstractComponent;
import net.akehurst.application.framework.components.Port;
import net.akehurst.application.framework.os.annotations.PortInstance;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemLocation;

public class DerbyDatabasePersistence extends AbstractComponent implements IPersistentStore {

	public DerbyDatabasePersistence(String id) {
		super(id);
	}

	@Override
	public void afRun() {
		try {
//			String driver = "org.apache.derby.jdbc.EmbeddedDriver";
//			Class.forName(driver);
			DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());

			this.conn = DriverManager.getConnection("jdbc:derby:db/ToDo;create=true");
			this.javaTypes = new HashMap<>();
			this.sqlTypes = new HashMap<>();
			this.addType(String.class, "LONG VARCHAR");
			this.addType(Integer.TYPE, "INTEGER");
			this.addType(Integer.class, "INTEGER");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	Connection conn;
	Map<String, Class<?>> javaTypes;
	Map<Class<?>, String> sqlTypes;

	void addType(Class<?> java, String sql) throws SQLException {
		this.javaTypes.put(sql, java);
		this.sqlTypes.put(java, sql);
	}

	<T> void createTable(Class<T> type) {
		try {
			String tableName = type.getSimpleName();
			if (this.tableExists(tableName)) {
				System.out.println("Table already exists " + tableName);
				this.addType(type, tableName);
				return;
			}

			String colDefs = "";
			for (Method m : type.getMethods()) {
				if (m.getDeclaringClass() != Object.class && m.getName().startsWith("get")) {
					Class<?> rt = m.getReturnType();
					String colName = m.getName().substring(3);
					String colType = sqlTypes.get(rt);
					if (null == colType) {
						Constructor c = rt.getConstructors()[0];
						if (c.getParameterTypes().length == 1) {
							rt = c.getParameters()[0].getType();
						} else {
							this.createTable(rt);
						}
						colType = sqlTypes.get(rt);
					} else {

					}
					colDefs += colName + " " + colType + ", ";
				} else {

				}
			}
			colDefs = colDefs.substring(0, colDefs.length() - 2); // remove last comma
			String sql = "CREATE TABLE " + tableName + " (" + colDefs + ")";
			this.conn.createStatement().executeUpdate(sql);
			this.addType(type, tableName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean tableExists(String tableName) {
		try {
			DatabaseMetaData dbmd = this.conn.getMetaData();
			String catalog = null;
			String schemaPattern = null;
			String tableNamePattern = tableName.toUpperCase();
			String[] types = null;
			ResultSet rs = dbmd.getTables(catalog, schemaPattern, tableNamePattern, types);
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	void addRow(String tableName, Map<String, Object> values) {
		try {
			String colNames = "";
			String valueStr = "";
			for (Map.Entry<String, Object> entry : values.entrySet()) {
				colNames += entry.getKey() + ", ";
				valueStr += "'" + entry.getValue() + "', ";
			}
			colNames = colNames.substring(0, colNames.length() - 2); // remove last comma
			valueStr = valueStr.substring(0, valueStr.length() - 2); // remove last comma
			String sql = "INSERT INTO " + tableName + "(" + colNames + ") VALUES (" + valueStr + ")";
			this.conn.createStatement().executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	Set<Map<String, Object>> fetchRows(String tableName, Map<String, Object> filter) {
		try {
			String whereClause = "";
			for (Map.Entry<String, Object> entry : filter.entrySet()) {
				whereClause += entry.getKey() + "=";
				whereClause += "'" + entry.getValue() + " AND ";
			}
			whereClause = whereClause.substring(0, whereClause.length() - 5); // remove the last AND
			String sql = "SELECT * FROM " + tableName;
			if (whereClause.isEmpty()) {
				sql += ";";
			} else {
				sql += " WHERE " + whereClause + ";";
			}
			ResultSet rs = this.conn.createStatement().executeQuery(sql);
			Set<Map<String, Object>> result = new HashSet<>();
			while (rs.next()) {
				ResultSetMetaData md = rs.getMetaData();
				int numCols = md.getColumnCount();
				HashMap<String, Object> m = new HashMap<>();
				for (int i = 1; i <= numCols; ++i) {
					String key = md.getColumnName(i);
					Object value = rs.getObject(i);
					m.put(key, value);
				}
				result.add(m);
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	<T> Map<String, Object> asMap(Class<T> type, T item) {
		try {
			Map<String, Object> result = new HashMap<>();

			for (Method m : type.getMethods()) {
				if (Object.class!=m.getDeclaringClass() && m.getName().startsWith("get")) {
					String key = m.getName().substring(3);
					Object value = m.invoke(item);

					result.put(key, value);
				} else {

				}
			}

			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	<T> T fromMap(Class<T> type, Map<String, Object> map) {
		try {
			Constructor<T> cons = (Constructor<T>) type.getConstructors()[0];
			ArrayList<Object> consArgs = new ArrayList<>();
			for (Parameter p : cons.getParameters()) {
				String key = p.getName();
				Object value = map.get(key);
				if (p.getType().isAssignableFrom(value.getClass())) {
					consArgs.add(value);
				} else {
					Constructor pc = p.getType().getConstructors()[0];
				}
			}
			T t = cons.newInstance(consArgs);
			return t;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public <T> void putItem(Class<T> type, T item) {
		String typeName = type.getSimpleName();
		if (this.tableExists(typeName)) {
			// ok
		} else {
			this.createTable(type);
		}
		Map<String, Object> map = this.asMap(type, item);
		this.addRow(typeName, map);
	}

	public <T> Set<T> getItems(Class<T> type, Map<String, Object> filter) {
		String typeName = type.getSimpleName();
		Set<Map<String, Object>> items = this.fetchRows(typeName, filter);

		Set<T> result = new HashSet<>();
		for (Map<String, Object> m : items) {
			T obj = (T) this.fromMap(type, m);
			result.add(obj);
		}

		return result;
	}

	public void putString(PersistentItemLocation id, String item) {

	}

	public String fetchString(PersistentItemLocation id) {
		return null;
	}

	// --------- IPersistentStore ---------
	
	@Override
	public void connect(Map<String, Object> properties) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public <T> void store(IPersistenceTransaction transaction, PersistentItemLocation location, T item, Class<T> itemType) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public <T> T retrieve(IPersistenceTransaction transaction,PersistentItemLocation location, Class<T> itemType) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public <T> Set<T> retrieve(IPersistenceTransaction transaction,PersistentItemLocation location, Class<T> itemType, Map<String, Object> filter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public <T> Set<T> retrieveAll(IPersistenceTransaction transaction,Class<T> itemType) {
		// TODO Auto-generated method stub
		return null;
	}
	
	// ---------- Ports ---------
	@PortInstance(provides={IPersistentStore.class},requires={})
	Port portPersist;

	public Port portPersist() {
		return this.portPersist;
	}

	@Override
	public IPersistenceTransaction startTransaction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void commitTransaction(IPersistenceTransaction transaction) {
		// TODO Auto-generated method stub
		
	}
}
