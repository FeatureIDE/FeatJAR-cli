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
package de.featjar.cli.analysis;

import de.featjar.analysis.sat4j.HasSolutionAnalysis;
import de.featjar.util.cli.AlgorithmWrapper;
import de.featjar.analysis.sat4j.*;
import de.featjar.util.cli.*;

public class VoidAlgorithm extends AlgorithmWrapper<HasSolutionAnalysis> {

	@Override
	protected HasSolutionAnalysis createAlgorithm() {
		return new HasSolutionAnalysis();
	}

	@Override
	public Object parseResult(Object result, Object arg) {
		return !((Boolean) result);
	}

	@Override
	public String getName() {
		return "void";
	}

	@Override
	public String getHelp() {
		final StringBuilder helpBuilder = new StringBuilder();
		helpBuilder.append("\t");
		helpBuilder.append(getName());
		helpBuilder.append(": reports whether the feature model has a valid configuration\n");
		return helpBuilder.toString();
	}
}
