/* -----------------------------------------------------------------------------
 * Command Line Interface - Reference frontend for the library
 * Copyright (C) 2021  Elias Kuiter
 * 
 * This file is part of Command Line Interface.
 * 
 * Command Line Interface is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Command Line Interface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Command Line Interface.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/cli> for further information.
 * -----------------------------------------------------------------------------
 */
package cli;

import java.util.*;

import org.junit.jupiter.api.*;
import org.spldev.cli.*;
import org.spldev.util.extension.*;

public class FormatConverterTest {
	@Test
	public void createAuxiliaryRoot() {
		ExtensionLoader.load();
		new FormatConverter().run(Arrays.asList("featureide_examples/FeatureModels/Linux_2.6.33.3",
			"org.spldev.formula.expression.io.DIMACSFormat", "-out", "test", "-r", "-f", "-name", "model[.]xml"));
	}
}
