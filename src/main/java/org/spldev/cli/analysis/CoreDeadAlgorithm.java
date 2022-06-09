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

import org.spldev.analysis.sat4j.CoreDeadAnalysis;
import org.spldev.clauses.LiteralList;
import org.spldev.formula.structure.atomic.literal.VariableMap;
import org.spldev.util.cli.AlgorithmWrapper;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CoreDeadAlgorithm extends AlgorithmWrapper<CoreDeadAnalysis> {

	@Override
	protected CoreDeadAnalysis createAlgorithm() {
		return new CoreDeadAnalysis();
	}

	@Override
	public Object parseResult(Object result, Object arg) {
		LiteralList literalList = (LiteralList) result;
		VariableMap variableMap = (VariableMap) arg;
		return String.format("core:\n%s\ndead:\n%s\n",
			Arrays.stream(literalList.getPositiveLiterals().getLiterals())
				.mapToObj(l -> variableMap.getVariable(l).get().getName())
				.collect(Collectors.joining("\n")),
			Arrays.stream(literalList.getNegativeLiterals().getLiterals())
				.mapToObj(l -> variableMap.getVariable(-l).get().getName())
				.collect(Collectors.joining("\n")));
	}

	@Override
	public String getName() {
		return "core-dead";
	}

	@Override
	public String getHelp() {
		final StringBuilder helpBuilder = new StringBuilder();
		helpBuilder.append("\t");
		helpBuilder.append(getName());
		helpBuilder.append(": reports the feature model's core and dead features\n");
		return helpBuilder.toString();
	}

}
