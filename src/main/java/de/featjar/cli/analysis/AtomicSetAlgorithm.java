/*
 * Copyright (C) 2023 Elias Kuiter
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
 */
package de.featjar.cli.analysis;

import de.featjar.analysis.sat4j.AtomicSetAnalysis;
import de.featjar.clauses.LiteralList;
import de.featjar.formula.structure.atomic.literal.VariableMap;
import de.featjar.util.cli.AlgorithmWrapper;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AtomicSetAlgorithm extends AlgorithmWrapper<AtomicSetAnalysis> {

    @Override
    protected AtomicSetAnalysis createAlgorithm() {
        return new AtomicSetAnalysis();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object parseResult(Object result, Object arg) {
        List<LiteralList> atomicSets = (List<LiteralList>) result;
        VariableMap variableMap = (VariableMap) arg;
        return atomicSets.stream()
                .map(atomicSet -> String.format(
                        "{%s}",
                        Stream.concat(
                                        Arrays.stream(atomicSet
                                                        .getPositiveLiterals()
                                                        .getLiterals())
                                                .mapToObj(l -> "+"
                                                        + variableMap
                                                                .getVariable(l)
                                                                .get()
                                                                .getName()),
                                        Arrays.stream(atomicSet
                                                        .getNegativeLiterals()
                                                        .getLiterals())
                                                .mapToObj(l -> "-"
                                                        + variableMap
                                                                .getVariable(-l)
                                                                .get()
                                                                .getName()))
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
