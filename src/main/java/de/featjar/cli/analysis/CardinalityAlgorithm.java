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

import de.featjar.base.data.Computation;
import de.featjar.formula.analysis.sharpsat.CountSolutionsAnalysis;
import de.featjar.base.cli.AlgorithmWrapper;
import de.featjar.formula.clauses.ToCNF;
import de.featjar.formula.structure.formula.Formula;

import java.util.function.Function;

public class CardinalityAlgorithm extends AlgorithmWrapper<Function<Formula, CountSolutionsAnalysis>> {

    @Override
    protected Function<Formula, CountSolutionsAnalysis> createAlgorithm() {
        return formula -> Computation.of(formula).then(ToCNF::new).then(CountSolutionsAnalysis::new);
    }

    @Override
    public String getName() {
        return "cardinality";
    }

    @Override
    public String getHelp() {
        final StringBuilder helpBuilder = new StringBuilder();
        helpBuilder.append("\t");
        helpBuilder.append(getName());
        helpBuilder.append(": reports the feature model's number of valid configurations\n");
        return helpBuilder.toString();
    }
}
