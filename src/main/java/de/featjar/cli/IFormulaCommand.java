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
package de.featjar.cli;

import de.featjar.base.cli.CommandLineInterface;
import de.featjar.base.cli.ICommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.StringOption;

public interface IFormulaCommand extends ICommand {
    Option<String> INPUT_OPTION = new StringOption("--input")
            .setDescription("Path to input formula file")
            .setDefaultValue(CommandLineInterface.STANDARD_INPUT);

    Option<String> OUTPUT_OPTION = new StringOption("--output")
            .setDescription("Path to output formula file")
            .setDefaultValue(CommandLineInterface.STANDARD_OUTPUT);
}
