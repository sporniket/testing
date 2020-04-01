package unit;

import static org.assertj.core.api.BDDAssertions.then;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.sporniket.libre.test.utils.ResourceHelper;

/**
 * Test suite for {@link ResourceHelper}.
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
 * @version 20.04.00
 * @since 20.04.00
 */
public class ResourceHelperTest
{
	@Test
	public void shouldFindExistingResource()
	{
		final InputStream _dataResource = ResourceHelper.getDataResource(this, "ok.txt");

		then(_dataResource).isNotNull();
	}

	@Test
	public void shouldNotUseTheClassOfJavaLangClass()
	{
		final InputStream _dataResource = ResourceHelper.getDataResource(this.getClass(), "ok.txt");

		then(_dataResource).isNotNull();
	}

	@Test
	public void shouldNotFindMissingResource()
	{
		final InputStream _dataResource = ResourceHelper.getDataResource(this, "bad.txt");

		then(_dataResource).isNull();
	}
}
