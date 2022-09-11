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

import de.featjar.analysis.sat4j.AbstractConfigurationGenerator;
import de.featjar.base.cli.AlgorithmWrapper;
import de.featjar.base.extension.ExtensionPoint;

/**
 * Extension point for sampling algorithms.
 *
 * @author Sebastian Krieter
 */
public class ConfigurationGeneratorAlgorithms
        extends ExtensionPoint<AlgorithmWrapper<? extends AbstractConfigurationGenerator>> {

    private static final ConfigurationGeneratorAlgorithms INSTANCE = new ConfigurationGeneratorAlgorithms();

    public static ConfigurationGeneratorAlgorithms getInstance() {
        return INSTANCE;
    }

    private ConfigurationGeneratorAlgorithms() {}

    @Override
    public ExtensionPoint<AlgorithmWrapper<? extends AbstractConfigurationGenerator>> getInstanceAsExtensionPoint() {
        return INSTANCE;
    }
}
