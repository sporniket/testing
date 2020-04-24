package com.sporniket.libre.test.dbunit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Mark that the class or the method will load a dataset.
 * 
 * <p>
 * By default, the dataset will be named "dataset.xml", and will be located in a data folder named after the annotated class and
 * method.
 * </p>
 * <ul>
 * <li>When annoting the class <code>test.my.SuperTest</code>, will load the dataset file
 * <code>/test/my/SuperTest_data/dataset.xml</code>.</li>
 * <li>When annoting the method <code>test.my.SuperTest.shouldDoSomethingGreat()</code>, will load the dataset file
 * <code>/test/my/SuperTest_data/dataset-shouldDoSomethingGreat.xml</code></li>
 * </ul>
 * 
 * <p>
 * By default, the dataset of the method will be appended in addition to the main dataset (if defined). If the <code>mode</code> is
 * set to {@link Mode#REPLACE}, then only the dataset of the method will be used.
 * </p>
 * 
 * @author dsporn
 *
 */
@Retention(RUNTIME)
@Target(
{
		TYPE, METHOD
})
public @interface Dataset
{
	public static enum Mode
	{
		APPEND,
		REPLACE;
	}

	String value() default "";

	Mode mode() default Mode.APPEND;

}
