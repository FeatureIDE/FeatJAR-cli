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
package de.featjar.cli;

import de.featjar.formula.analysis.sat4j.configuration.AbstractConfigurationGenerator;
import de.featjar.formula.clauses.solutions.SolutionList;
import de.featjar.formula.clauses.solutions.io.ListFormat;
import de.featjar.cli.configuration.ConfigurationGeneratorAlgorithms;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.base.cli.AlgorithmWrapper;
import de.featjar.base.cli.CommandLine;
import de.featjar.base.cli.Command;
import de.featjar.base.data.Result;
import de.featjar.base.log.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Command line interface for sampling algorithms.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class ConfigurationGenerator implements Command {

    private final List<AlgorithmWrapper<? extends AbstractConfigurationGenerator>> algorithms =
            ConfigurationGeneratorAlgorithms.getInstance().getExtensions();

    @Override
    public String getName() {
        return "genconfig";
    }

    @Override
    public String getDescription() {
        return "Generates configurations with various sampling algorithms";
    }

    @Override
    public void run(List<String> args) {
        String input = CommandLine.SYSTEM_INPUT;
        String output = CommandLine.SYSTEM_OUTPUT;
        AlgorithmWrapper<? extends AbstractConfigurationGenerator> algorithm = null;
        int limit = Integer.MAX_VALUE;
        String verbosity = CommandLine.DEFAULT_MAXIMUM_VERBOSITY;

        final List<String> remainingArguments = new ArrayList<>();
        for (final ListIterator<String> iterator = args.listIterator(); iterator.hasNext(); ) {
            final String arg = iterator.next();
            switch (arg) {
                case "-a": {
                    // TODO add plugin for icpl and chvatal
                    final String name = CommandLine.getArgValue(iterator, arg).toLowerCase();
                    algorithm = algorithms.stream()
                            .filter(a -> Objects.equals(name, a.getName()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Unknown algorithm: " + name));
                    break;
                }
                case "-o": {
                    output = CommandLine.getArgValue(iterator, arg);
                    break;
                }
                case "-i": {
                    input = CommandLine.getArgValue(iterator, arg);
                    break;
                }
                case "-v": {
                    verbosity = CommandLine.getArgValue(iterator, arg);
                    break;
                }
                case "-l":
                    limit = Integer.parseInt(CommandLine.getArgValue(iterator, arg));
                    break;
                default: {
                    remainingArguments.add(arg);
                    break;
                }
            }
        }

        CommandLine.installLog(verbosity);

        if (algorithm == null) {
            throw new IllegalArgumentException("No algorithm specified!");
        }
        final AbstractConfigurationGenerator generator =
                algorithm.parseArguments(remainingArguments).orElse(Log::problems);
        if (generator != null) {
            generator.setLimit(limit);
            final ModelRepresentation c = CommandLine.loadFile(input, FormulaFormats.getInstance()) //
                    .map(ModelRepresentation::new) //
                    .orElseThrow(p -> new IllegalArgumentException(
                            p.isEmpty() ? null : p.get(0).toException()));
            final Result<SolutionList> result = c.getResult(generator);
            String finalOutput = output;
            result.ifPresentOrElse(list -> CommandLine.saveFile(list, finalOutput, new ListFormat()), Log::problems);
        }
    }

    @Override
    public String getUsage() {
        final StringBuilder helpBuilder = new StringBuilder();
        helpBuilder.append("\tGeneral Parameters:\n");
        helpBuilder.append("\t\t-i <Path>    Specify path to input feature model file (default: system:in.xml>)\n");
        helpBuilder.append("\t\t-o <Path>    Specify path to output CSV file (default: system:out>)\n");
        helpBuilder.append("\t\t-v <Level>   Specify verbosity. One of: none, error, info, debug, progress\n");
        helpBuilder.append("\t\t-a <Name>    Specify algorithm by name. One of:\n");
        algorithms.forEach(a ->
                helpBuilder.append("\t\t                 ").append(a.getName()).append("\n"));
        helpBuilder.append("\n");
        helpBuilder.append("\tAlgorithm Specific Parameters:\n\t");
        algorithms.forEach(a -> helpBuilder.append(a.getHelp().replace("\n", "\n\t")));
        return helpBuilder.toString();
    }
}
