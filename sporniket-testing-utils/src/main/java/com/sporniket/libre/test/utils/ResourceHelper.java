/**
 * 
 */
package com.sporniket.libre.test.utils;

import static java.lang.String.format;

import java.io.File;
import java.io.InputStream;

/**
 * Helpers to load a ressource located in a folder next to the source class.
 * 
 * <p>
 * For a class with the full name <code>com.foo.GreatTest</code>, the files will be located in a folder named
 * <code>com/foo/GreatTest_data</code>, starting from the classloader root.
 * </p>
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
 * @version 20.04.01
 * @since 20.04.00
 */
public class ResourceHelper
{
	private static final String FORMAT__DATA_FILE_PATH = "%s_data/%s";

	/**
	 * Load a resource located in a folder next to the location of the client class.
	 * 
	 * <p>
	 * For a client that is an instance of a class with the full name <code>com.foo.GreatTest</code>, the files will be located in a
	 * folder named <code>com/foo/GreatTest_data</code>, starting from the classloader root.
	 * </p>
	 * 
	 * @param client
	 *            either the object, or the class of the object.
	 * @param resourceName
	 *            the name of the file
	 * @return an input stream from the file, or <code>null</code> if the file was not found.
	 */
	public static InputStream getDataResource(final Object client, final String resourceName)
	{
		final Class<? extends Object> _class = (client instanceof Class<?>) ? (Class<?>) client : client.getClass();
		return getDataResourceForClass(_class, resourceName);
	}

	/**
	 * Load a resource located in a folder next to the location of the given class.
	 * 
	 * 
	 * <p>
	 * For a class with the full name <code>com.foo.GreatTest</code>, the files will be located in a folder named
	 * <code>com/foo/GreatTest_data</code>, starting from the classloader root.
	 * </p>
	 * <p>
	 * This will be the only way to load a resource file for <code>java.lang.Class</code>.
	 * </p>
	 * 
	 * @param clientClass
	 *            the class of the client object.
	 * @param resourceName
	 *            the name of the file
	 * @return an input stream from the file, or <code>null</code> if the file was not found.
	 */
	public static InputStream getDataResourceForClass(final Class<? extends Object> clientClass, String resourceName)
	{
		final String _name = clientClass.getName();
		final String _path = format(FORMAT__DATA_FILE_PATH, _name.replace('.', File.separatorChar), resourceName);
		return clientClass.getClassLoader().getResourceAsStream(_path);
	}

}
