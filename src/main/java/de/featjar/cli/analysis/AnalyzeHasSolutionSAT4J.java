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

import de.featjar.base.computation.IComputation;
import de.featjar.formula.analysis.VariableMap;
import de.featjar.formula.analysis.bool.BooleanClauseList;


public class AnalyzeHasSolutionSAT4J extends ASAT4JAnalysisCommand<Boolean, Boolean> {
    @Override
    public String getDescription() {
        return "Queries SAT4J for whether a given formula has a solution";
    }

    @Override
    public de.featjar.formula.analysis.sat4j.AnalyzeHasSolutionSAT4J newAnalysis(IComputation<BooleanClauseList> clauseList) {
        return new de.featjar.formula.analysis.sat4j.AnalyzeHasSolutionSAT4J(clauseList);
    }

    @Override
    public IComputation<Boolean> interpret(IComputation<Boolean> bool, IComputation<VariableMap> variableMap) {
        return bool;
    }
}
