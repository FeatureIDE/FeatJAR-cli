/*
 * Copyright (C) 2023 Elias Kuiter
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
 * See <https://github.com/FeatJAR/cli> for further information.
 */
package de.featjar.cli;

import de.featjar.analysis.Analysis;
import de.featjar.cli.analysis.AnalysisAlgorithmManager;
import de.featjar.formula.ModelRepresentation;
import de.featjar.formula.io.FormulaFormatManager;
import de.featjar.util.cli.AlgorithmWrapper;
import de.featjar.util.cli.CLI;
import de.featjar.util.cli.CLIFunction;
import de.featjar.util.logging.Logger;
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
public class FormulaAnalyzer implements CLIFunction {
    private final List<AlgorithmWrapper<Analysis<?>>> algorithms =
            AnalysisAlgorithmManager.getInstance().getExtensions();

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
        String input = CLI.SYSTEM_INPUT;
        AlgorithmWrapper<Analysis<?>> algorithm = null;
        long timeout = 0;
        String verbosity = CLI.DEFAULT_VERBOSITY;

        final List<String> remainingArguments = new ArrayList<>();
        for (final ListIterator<String> iterator = args.listIterator(); iterator.hasNext(); ) {
            final String arg = iterator.next();
            switch (arg) {
                case "-a": {
                    final String name = CLI.getArgValue(iterator, arg).toLowerCase();
                    algorithm = algorithms.stream()
                            .filter(a -> Objects.equals(name, a.getName()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Unknown algorithm: " + name));
                    break;
                }
                case "-i": {
                    input = CLI.getArgValue(iterator, arg);
                    break;
                }
                case "-t": {
                    timeout = Long.parseLong(CLI.getArgValue(iterator, arg));
                    break;
                }
                case "-v": {
                    verbosity = CLI.getArgValue(iterator, arg);
                    break;
                }
                default: {
                    remainingArguments.add(arg);
                    break;
                }
            }
        }

        CLI.installLogger(verbosity);

        if (algorithm == null) {
            throw new IllegalArgumentException("No algorithm specified!");
        }

        final ModelRepresentation rep = CLI.loadFile(input, FormulaFormatManager.getInstance())
                .map(ModelRepresentation::new)
                .orElseThrow();
        final Analysis<?> analysis =
                algorithm.parseArguments(remainingArguments).orElse(Logger::logProblems);

        final long localTime = System.nanoTime();
        final Object result =
                CLI.runInThread(() -> rep.getResult(analysis), timeout).orElse(Logger::logProblems);
        final long timeNeeded = System.nanoTime() - localTime;

        Logger.logInfo("Time:\n" + ((timeNeeded / 1_000_000) / 1000.0) + "s");
        Logger.logInfo(
                "Result:\n" + algorithm.parseResult(result, rep.getFormula().getVariableMap()));
    }

    @Override
    public String getHelp() {
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
