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
package de.featjar.cli.configuration;

import de.featjar.formula.analysis.sat4j.todo.configuration.FastRandomConfigurationGenerator;

/**
 * Generates random configurations for a given propositional formula.
 *
 * @author Sebastian Krieter
 */
public class FastRandomAlgorithm extends ARandomAlgorithm {

    @Override
    protected FastRandomConfigurationGenerator newAlgorithm() {
        return new FastRandomConfigurationGenerator();
    }

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public String getHelp() {
        final StringBuilder helpBuilder = new StringBuilder();
        helpBuilder.append("\t");
        helpBuilder.append(getName());
        helpBuilder.append(": generates random valid configurations (not guaranteed to be uniformly distributed)\n");
        helpBuilder.append("\t\t-l <Value>    Specify maximum number of configurations\n");
        helpBuilder.append("\t\t-s <Value>    Specify random seed\n");
        return helpBuilder.toString();
    }
}
