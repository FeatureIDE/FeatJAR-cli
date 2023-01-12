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

import de.featjar.base.computation.IComputation;
import de.featjar.base.io.format.IFormat;
import de.featjar.formula.analysis.VariableMap;
import de.featjar.formula.analysis.bool.BooleanClauseList;
import de.featjar.formula.structure.formula.IFormula;

import java.math.BigInteger;

public class ComputeSolutionCountSharpSAT extends ASharpSATAnalysisCommand<BigInteger, BigInteger> {
    @Override
    public String getDescription() {
        return "Queries SharpSAT for the number of solutions of a given formula";
    }

    @Override
    public de.featjar.formula.analysis.sharpsat.ComputeSolutionCountSharpSAT newAnalysis(
            IComputation<IFormula> cnfFormula) {
        return new de.featjar.formula.analysis.sharpsat.ComputeSolutionCountSharpSAT(cnfFormula);
    }

    @Override
    public IComputation<BigInteger> interpret(IComputation<BigInteger> count) {
        return count;
    }
}
