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

import org.spldev.analysis.sat4j.AtomicSetAnalysis;
import org.spldev.clauses.LiteralList;
import org.spldev.formula.structure.atomic.literal.VariableMap;
import org.spldev.util.cli.AlgorithmWrapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AtomicSetAlgorithm extends AlgorithmWrapper<AtomicSetAnalysis> {

	@Override
	protected AtomicSetAnalysis createAlgorithm() {
		return new AtomicSetAnalysis();
	}

	@Override
	public Object parseResult(Object result, Object arg) {
		List<LiteralList> atomicSets = (List<LiteralList>) result;
		VariableMap variableMap = (VariableMap) arg;
		return atomicSets.stream().map(atomicSet -> String.format("{%s}",
			Stream.concat(Arrays.stream(atomicSet.getPositiveLiterals().getLiterals())
				.mapToObj(l -> "+" + variableMap.getVariable(l).get().getName()),
				Arrays.stream(atomicSet.getNegativeLiterals().getLiterals())
					.mapToObj(l -> "-" + variableMap.getVariable(-l).get().getName()))
				.collect(Collectors.joining(", "))))
			.collect(Collectors.joining("\n"));
	}

	@Override
	public String getName() {
		return "atomic-sets";
	}

	@Override
	public String getHelp() {
		final StringBuilder helpBuilder = new StringBuilder();
		helpBuilder.append("\t");
		helpBuilder.append(getName());
		helpBuilder.append(": reports the feature model's atomic sets\n");
		return helpBuilder.toString();
	}

}
