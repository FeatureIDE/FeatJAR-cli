/*
 * Copyright (C) 2023 Elias Kuiter
 *
 * This file is part of FeatJAR-cli.
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

import de.featjar.base.cli.ICommand;
import de.featjar.base.cli.Option;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.analysis.sharpsat.ASharpSATAnalysis;
import de.featjar.formula.structure.formula.IFormula;
import de.featjar.formula.transformer.ComputeCNFFormula;
import de.featjar.formula.transformer.ComputeNNFFormula;

import java.util.List;

import static de.featjar.base.computation.Computations.*;

public abstract class ASharpSATAnalysisCommand<T, U> extends AAnalysisCommand<T> {
    @Override
    public List<Option<?>> getOptions() {
        return ICommand.addOptions(super.getOptions(), TIMEOUT_OPTION);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IComputation<T> newComputation() {
        var cnfFormula = async(formula)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new);
        var analysis = newAnalysis(cnfFormula);
        analysis.setTimeout(async(optionParser.get(TIMEOUT_OPTION)));
        return interpret(analysis);
    }

    public abstract ASharpSATAnalysis<U> newAnalysis(IComputation<IFormula> cnfFormula);

    public abstract IComputation<T> interpret(IComputation<U> result);
}
