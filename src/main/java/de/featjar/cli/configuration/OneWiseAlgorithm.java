/* -----------------------------------------------------------------------------
 * cli -  Command Line Interface
 * Copyright (C) 2021 Elias Kuiter
 * 
 * This file is part of cli.
 * 
 * cli is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 * 
 * cli is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with cli. If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/FeatJAR/cli> for further information.
 * -----------------------------------------------------------------------------
 */
package de.featjar.cli.configuration;

import de.featjar.analysis.sat4j.AbstractConfigurationGenerator;
import de.featjar.analysis.sat4j.OneWiseConfigurationGenerator;
import de.featjar.util.cli.AlgorithmWrapper;
import de.featjar.analysis.sat4j.*;
import de.featjar.util.cli.*;

/**
 * Generates configurations for a given propositional formula such that one-wise
 * feature coverage is achieved.
 *
 * @author Sebastian Krieter
 */
public class OneWiseAlgorithm extends AlgorithmWrapper<AbstractConfigurationGenerator> {

	@Override
	protected OneWiseConfigurationGenerator createAlgorithm() {
		return new OneWiseConfigurationGenerator();
	}

	@Override
	public String getName() {
		return "onewise";
	}

	@Override
	public String getHelp() {
		final StringBuilder helpBuilder = new StringBuilder();
		helpBuilder.append("\t");
		helpBuilder.append(getName());
		helpBuilder.append(
			": generates a set of valid configurations such that one-wise feature coverage is achieved\n");
		helpBuilder.append("\t\t-l <Value>    Specify maximum number of configurations\n");
		return helpBuilder.toString();
	}

}
