/*
 * Copyright (C) 2022 Elias Kuiter
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
 * See <https://github.com/FeatureIDE/FeatJAR-cli> for further information.
 */
package de.featjar.cli.analysis;

import de.featjar.base.cli.Option;
import de.featjar.base.data.Computation;
import de.featjar.formula.analysis.bool.BooleanAssignment;
import de.featjar.formula.analysis.bool.BooleanSolution;
import de.featjar.formula.analysis.bool.ToBooleanClauseList;
import de.featjar.formula.analysis.sat4j.SAT4JGetSolutionAnalysis;
import de.featjar.formula.transformer.ToCNF;

import java.util.List;

import static de.featjar.base.data.Computations.async;


public class SAT4JGetSolution extends AnalysisCommand<BooleanSolution> {
    @Override
    public String getDescription() {
        return "Queries SAT4J for a solution of a given formula, if any";
    }

    @Override
    public List<Option<?>> getOptions() {
        return List.of(INPUT_OPTION, ASSIGNMENT_OPTION, CLAUSES_OPTION, TIMEOUT_OPTION);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SAT4JGetSolutionAnalysis newAnalysis() {
        var pair =
                async(formula)
                        .map(ToCNF::new)
                        .map(ToBooleanClauseList::new);
        var booleanClauseList = pair.getKey();
        var variableMap = pair.getValue();
        return new SAT4JGetSolutionAnalysis()
                .setInput(booleanClauseList)
                .setAssumedAssignment((Computation<BooleanAssignment>) ASSIGNMENT_OPTION.parseFrom(argumentParser).toBoolean(variableMap))
                .setAssumedClauseList(CLAUSES_OPTION.parseFrom(argumentParser).toBoolean(variableMap))
                .setTimeout(TIMEOUT_OPTION.parseFrom(argumentParser));
    }

    @Override
    public String serializeResult(BooleanSolution result) {
        return result.print();
    }
}
