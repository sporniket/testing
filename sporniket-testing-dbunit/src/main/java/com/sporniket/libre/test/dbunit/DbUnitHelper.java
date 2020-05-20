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
 * @author dsporn
 *
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
