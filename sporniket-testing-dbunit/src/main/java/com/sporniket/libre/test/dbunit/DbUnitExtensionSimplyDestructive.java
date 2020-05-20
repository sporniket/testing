package com.sporniket.libre.test.dbunit;

import static com.sporniket.libre.test.dbunit.DbUnitHelper.createConnection;
import static com.sporniket.libre.test.utils.ResourceHelper.getDataResourceForClass;
import static java.lang.String.format;
import static org.dbunit.operation.DatabaseOperation.CLEAN_INSERT;
import static org.dbunit.operation.DatabaseOperation.DELETE_ALL;
import static org.dbunit.operation.DatabaseOperation.INSERT;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Very crude JUnit5 extension tu use dbunit, taking advantage of a non persisted database in a ditchable docker container.
 *
 * <p>
 * DO NOT USE IF THIS EXTENSION IF YOUR BASE CONTAINS REAL DATA THAT YOU WANT TO PRESERVE
 * </p>
 *
 * <p>
 * A <code>dbunit.properties</code> MUST be at the root of the resource folder, with the following properties :
 * </p>
 *
 * <ul>
 * <li>url : jdbc url, e.g. 'jdbc:postgresql://localhost:54320/postgres'.</li>
 * <li>driverClass : jdbc url, e.g. 'org.postgresql.Driver'.</li>
 * <li>username : name of the database user, should have access to the schema.</li>
 * <li>password : password for the database user.</li>
 * <li>schema : name of the schema to use.</li>
 * </ul>
 *
 * <p>
 * For each test class, e.g. <code>com.foo.MyTest</code>, the extension try to load the xml dataset at this location :
 * <code>classpath:/com/foo/MyTest_data/dataset.xml</code>.
 * </p>
 *
 * <p>
 * For each test method of the test class, e.g. <code>shouldPass(...)</code>, the extension try to load the xml dataset
 * <em>dataset-[method name].xml</em> (e.g. thus at this location :
 * <code>classpath:/com/foo/MyTest_data/dataset-shouldPass.xml</code>).
 * </p>
 *
 * <p>
 * Before each test method, the general dataset is inserted, and the specific dataset is appended.
 * </p>
 *
 *
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
 * @since 20.04.01
 */
public class DbUnitExtensionSimplyDestructive
		implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback, TestExecutionExceptionHandler
{
	private static Connection con = null;

	private static Properties setupProps = null;

	private static final String DATASET_XML__MAIN = "dataset.xml";

	private static final String DATASET_XML__METHOD = "dataset-%s.xml";

	private static final Logger LOGGER = LoggerFactory.getLogger(DbUnitExtensionSimplyDestructive.class);

	public static synchronized Properties getConfiguration() throws IOException
	{
		if (null == setupProps)
		{
			Properties _setupProps = new Properties();
			_setupProps.load(DbUnitExtensionSimplyDestructive.class.getResourceAsStream("/dbunit.properties"));
			setupProps = _setupProps;
		}
		return setupProps;
	}

	public static synchronized Connection getConnection()
			throws IOException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		if (con == null)
		{
			Properties _setupProps = getConfiguration();

			Properties connProps = new Properties();
			connProps.put("user", _setupProps.getProperty("username"));
			connProps.put("password", _setupProps.getProperty("password"));

			// create connection
			Driver driver = (Driver) Class.forName(_setupProps.getProperty("driverClass")).getConstructor().newInstance();
			con = driver.connect(_setupProps.getProperty("url"), connProps);
		}
		return con;
	}

	FlatXmlDataSet additionalDataset = null;

	boolean alreadyRolledBack;

	private DatabaseOperation append = INSERT;

	private Class<?> currentClass = null;

	private DatabaseConnection dbUnitConnection;

	private DatabaseOperation delete = DELETE_ALL;

	private DatabaseOperation insert = CLEAN_INSERT;

	private FlatXmlDataSet mainDataSet = null;

	private String schemaName = null;

	@Override
	public void afterAll(ExtensionContext context) throws Exception
	{
		LOGGER.debug("DbUnitExtensionSimplyDestructive -- afterAll");
		if (con != null)
		{
			LOGGER.debug("Release connexion");
			dbUnitConnection = null;
			con.close();
			con = null;
		}

	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception
	{
		LOGGER.debug("DbUnitExtensionSimplyDestructive -- afterEach");
		doRollback();
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
		LOGGER.debug("DbUnitExtensionSimplyDestructive -- beforeAll");
		LOGGER.debug("Load general dataset if exists");
		final Optional<Class<?>> _testClass = context.getTestClass();
		if (_testClass.isPresent())
		{
			currentClass = _testClass.get();
			mainDataSet = loadDataset(DATASET_XML__MAIN);
		}
		else
		{
			LOGGER.debug("Context class not found, ignore.");
		}
		LOGGER.debug("Establish connection");
		con = getConnection();
		Properties configuration = getConfiguration();
		schemaName = configuration.getProperty("schema");
		dbUnitConnection = createConnection(con, schemaName, configuration);
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception
	{
		LOGGER.debug("DbUnitExtensionSimplyDestructive -- beforeEach");
		LOGGER.warn("display name {}", context.getDisplayName());
		final Optional<Method> _testMethod = context.getTestMethod();
		if (_testMethod.isPresent())
		{
			final Method _method = _testMethod.get();
			additionalDataset = loadDataset(format(DATASET_XML__METHOD, _method.getName()));
		}
		doSavepoint();
		doApplyDatasets();
	}

	private void doApplyDatasets() throws DatabaseUnitException, SQLException
	{
		DatabaseOperation _currentOperation = insert;
		if (null != mainDataSet)
		{
			LOGGER.debug("Apply main dataset.");
			_currentOperation.execute(dbUnitConnection, mainDataSet);
			_currentOperation = append;
		}
		if (null != additionalDataset)
		{
			LOGGER.debug("Apply additionnal dataset.");
			_currentOperation.execute(dbUnitConnection, additionalDataset);
		}
	}

	private synchronized void doRollback() throws SQLException, DatabaseUnitException
	{
		LOGGER.debug("---> rollback");
		if (!alreadyRolledBack)
		{
			LOGGER.debug("rollback effectif !");
			try
			{
				if (null != additionalDataset)
				{
					delete.execute(dbUnitConnection, additionalDataset);
					additionalDataset = null;
				}
				if (null != mainDataSet)
				{
					delete.execute(dbUnitConnection, mainDataSet);
				}
			}
			catch (Exception _error)
			{
				_error.printStackTrace();
			}
			alreadyRolledBack = true;
		}
		LOGGER.debug("<--- rollback");
	}

	private synchronized void doSavepoint() throws SQLException
	{
		alreadyRolledBack = false;
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable
	{
		LOGGER.debug("DbUnitExtensionSimplyDestructive -- handleTestExecutionException");
		doRollback();
		throw throwable;
	}

	private FlatXmlDataSet loadDataset(final String datasetName) throws DataSetException
	{
		if (null == currentClass)
		{
			return null;
		}
		final InputStream _dataResourceForClass = getDataResourceForClass(currentClass, datasetName);
		return (null != _dataResourceForClass)
				? new FlatXmlDataSetBuilder().setColumnSensing(true).build(_dataResourceForClass)
				: null;
	}

}
