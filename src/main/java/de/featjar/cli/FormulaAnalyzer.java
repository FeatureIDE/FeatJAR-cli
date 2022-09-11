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

import de.featjar.formula.analysis.Analysis;
import de.featjar.cli.analysis.AnalysisAlgorithms;
import de.featjar.formula.ModelRepresentation;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.base.cli.AlgorithmWrapper;
import de.featjar.base.cli.CommandLine;
import de.featjar.base.cli.Command;
import de.featjar.base.log.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Command line interface for analyses on feature models.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class FormulaAnalyzer implements Command {
    private final List<AlgorithmWrapper<Analysis<?>>> algorithms =
            AnalysisAlgorithms.getInstance().getExtensions();

    @Override
    public String getName() {
        return "analyze";
    }

    @Override
    public String getDescription() {
        return "Performs an analysis on a feature model";
    }

    @Override
    public void run(List<String> args) {
        String input = CommandLine.SYSTEM_INPUT;
        AlgorithmWrapper<Analysis<?>> algorithm = null;
        long timeout = 0;
        String verbosity = CommandLine.DEFAULT_MAXIMUM_VERBOSITY;

        final List<String> remainingArguments = new ArrayList<>();
        for (final ListIterator<String> iterator = args.listIterator(); iterator.hasNext(); ) {
            final String arg = iterator.next();
            switch (arg) {
                case "-a": {
                    final String name = CommandLine.getArgValue(iterator, arg).toLowerCase();
                    algorithm = algorithms.stream()
                            .filter(a -> Objects.equals(name, a.getName()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Unknown algorithm: " + name));
                    break;
                }
                case "-i": {
                    input = CommandLine.getArgValue(iterator, arg);
                    break;
                }
                case "-t": {
                    timeout = Long.parseLong(CommandLine.getArgValue(iterator, arg));
                    break;
                }
                case "-v": {
                    verbosity = CommandLine.getArgValue(iterator, arg);
                    break;
                }
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

        final ModelRepresentation rep = CommandLine.loadFile(input, FormulaFormats.getInstance())
                .map(ModelRepresentation::new)
                .orElseThrow();
        final Analysis<?> analysis =
                algorithm.parseArguments(remainingArguments).orElse(Log::problems);

        final long localTime = System.nanoTime();
        final Object result =
                CommandLine.runInThread(() -> rep.getResult(analysis), timeout).orElse(Log::problems);
        final long timeNeeded = System.nanoTime() - localTime;

        Feat.log().info("Time:\n" + ((timeNeeded / 1_000_000) / 1000.0) + "s");
        Feat.log().info(
                "Result:\n" + algorithm.parseResult(result, rep.getFormula().getVariableMap()));
    }

    @Override
    public String getUsage() {
        final StringBuilder helpBuilder = new StringBuilder();
        helpBuilder.append("\tGeneral Parameters:\n");
        helpBuilder.append("\t\t-i <Path>    Specify path to feature model file (default: system:in.xml)\n");
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
