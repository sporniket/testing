/**
 *
 */
package com.sporniket.libre.test.dbunit;

import static java.lang.String.format;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;

/**
 * Utility classes for using dbUnit.
 *
 * <p>
 * &copy; Copyright 2020 David Sporn
 * </p>
 * <hr>
 *
 * <p>
 * This file is part of <i>The Sporniket Testing Library &#8211; utils</i>.
 *
 * <p>
 * <i>The Sporniket Testing Library &#8211; utils</i> is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * <p>
 * <i>The Sporniket Testing Library &#8211; utils</i> is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with <i>The Sporniket Testing Library &#8211;
 * utils</i>. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>. 2
 *
 * <hr>
 *
 * @author David SPORN
 * @version 20.05.00
 * @since 20.05.00
 */
class DbUnitHelper
{

	private static final String SQL_ENUMS_BY_SCHEMA = "SELECT pg_namespace.nspname as schema, pg_type.typname AS enum_type, count(pg_enum.enumtypid) AS enum_value FROM pg_type JOIN pg_enum ON pg_enum.enumtypid = pg_type.oid JOIN pg_namespace ON pg_namespace.oid = pg_type.typnamespace group by pg_namespace.nspname, pg_type.typname order by pg_namespace.nspname, pg_type.typname";

	static DatabaseConnection createConnection(Connection con, String schemaName, Properties configuration)
			throws DatabaseUnitException, IOException
	{
		DatabaseConnection dbuCon = new DatabaseConnection(con, schemaName);
		if (configuration.getProperty("url").startsWith("jdbc:postgresql"))
		{
			try
			{
				final Set<String> enums = findEnums(con, schemaName);
				DatabaseConfig config = dbuCon.getConfig();
				config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory()
				{
					@Override
					public boolean isEnumType(String sqlTypeName)
					{
						return enums.contains(sqlTypeName);
					}

					@Override
					public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException
					{
						int sqlTypeActual = (isEnumType(sqlTypeName)) ? Types.OTHER : sqlType;
						return super.createDataType(sqlTypeActual, sqlTypeName);
					}

				});
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dbuCon;
	}

	private static Set<String> findEnums(Connection con, String schemaName) throws SQLException
	{
		final Set<String> enums = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		for (ResultSet r = con.prepareCall(SQL_ENUMS_BY_SCHEMA).executeQuery(); r.next();)
		{
			final String _schema = r.getString("schema");
			final String _name = r.getString("enum_type");
			if (_schema.equals(schemaName))
			{
				String _enumName = format("\"%s\".\"%s\"", _schema, _name);
				enums.add(_enumName);
			}
		}
		return enums;
	}

}
