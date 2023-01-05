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
package de.featjar.cli.io;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.*;
import de.featjar.cli.IFormulaCommand;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.structure.formula.IFormula;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.formula.transformation.ComputeCNFFormula;
import de.featjar.formula.transformation.ComputeNNFFormula;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.featjar.base.computation.Computations.async;

/**
 * ...
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class FormatConverter implements IFormulaCommand {
    public static final Option<String> OUTPUT_FORMAT_OPTION = new StringOption("--format")
            .setRequired(true)
            .setDescription(() -> "Specify format by identifier. One of " +
                    getFormats().stream()
                            .map(IFormat::getName)
                            .map(String::toLowerCase)
                            .collect(Collectors.joining(", ")));

    public static final Option<Boolean> DRY_RUN_OPTION = new Flag("--dry-run")
            .setDescription("Perform dry run");

    public static final Option<Boolean> RECURSIVE_OPTION = new Flag("--recursive")
            .setDescription("");

    public static final Option<Boolean> CNF_OPTION = new Flag("--cnf")
            .setDescription("Transform into CNF before conversion");

    @Override
    public List<Option<?>> getOptions() {
        return List.of(INPUT_OPTION, OUTPUT_OPTION, OUTPUT_FORMAT_OPTION, DRY_RUN_OPTION, RECURSIVE_OPTION);
    }

    @Override
    public String getDescription() {
        return "Converts formulas from one format to another";
    }

    protected static List<IFormat<IFormula>> getFormats() {
        return FeatJAR.extensionPoint(FormulaFormats.class).getExtensions();
    }

    @Override
    public void run(ArgumentParser argumentParser) {
        String input = INPUT_OPTION.parseFrom(argumentParser).get();
        String output = OUTPUT_OPTION.parseFrom(argumentParser).get();
        String outputFormatString = OUTPUT_FORMAT_OPTION.parseFrom(argumentParser).get();
        IFormat<IFormula> outputFormat = getFormats().stream() // todo: find by extension ID
                .filter(f -> Objects.equals(outputFormatString, f.getName().toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown format: " + outputFormatString));
        boolean dryRun = DRY_RUN_OPTION.parseFrom(argumentParser).get();
        boolean recursive = RECURSIVE_OPTION.parseFrom(argumentParser).get();
        boolean CNF = CNF_OPTION.parseFrom(argumentParser).get();
        if (!CommandLineInterface.isValidInput(input)) {
            throw new IllegalArgumentException("input file invalid");
        }
        FeatJAR.log().info("converting " + input + " into " + outputFormatString);
        if (dryRun) {
            FeatJAR.log().debug("skipping due to dry run");
        } else {
            convert(input, output, outputFormat, CNF);
        }
    }

    private void convert(String input, String output, IFormat<IFormula> outputFormat, boolean CNF) {
        try {
            final Result<IFormula> formula = CommandLineInterface.loadFile(input, FeatJAR.extensionPoint(FormulaFormats.class));
            if (!formula.isPresent()) {
                FeatJAR.log().error("formula file could not be parsed");
            }
            if (formula.hasProblem())
                FeatJAR.log().problem(formula.getProblem().get());
            IFormula expression = formula.get();
            if (CNF) {
                expression = async(expression)
                        .map(ComputeNNFFormula::new)
                        .map(ComputeCNFFormula::new).getResult().get();
            }
            FeatJAR.log().debug(expression.print());
            CommandLineInterface.saveFile(expression, output, outputFormat);
        } catch (final Exception e) {
            FeatJAR.log().error(e);
        }
    }
}
