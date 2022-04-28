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
package org.spldev.cli.analysis;

import org.spldev.analysis.*;
import org.spldev.util.cli.*;
import org.spldev.util.extension.*;

/**
 * Extension point for analysis algorithms.
 *
 * @author Sebastian Krieter
 */
public class AnalysisAlgorithmManager extends ExtensionPoint<AlgorithmWrapper<Analysis<?>>> {

	private static final AnalysisAlgorithmManager INSTANCE = new AnalysisAlgorithmManager();

	public static final AnalysisAlgorithmManager getInstance() {
		return INSTANCE;
	}

	private AnalysisAlgorithmManager() {
	}

}
