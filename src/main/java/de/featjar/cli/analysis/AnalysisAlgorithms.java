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

import de.featjar.formula.analysis.Analysis;
import de.featjar.base.cli.AlgorithmWrapper;
import de.featjar.base.extension.ExtensionPoint;

/**
 * Extension point for analysis algorithms.
 *
 * @author Sebastian Krieter
 */
public class AnalysisAlgorithms extends ExtensionPoint<AlgorithmWrapper<Analysis<?>>> {

    private static final AnalysisAlgorithms INSTANCE = new AnalysisAlgorithms();

    public static AnalysisAlgorithms getInstance() {
        return INSTANCE;
    }

    private AnalysisAlgorithms() {}

    @Override
    public ExtensionPoint<AlgorithmWrapper<Analysis<?>>> getInstanceAsExtensionPoint() {
        return INSTANCE;
    }
}
