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

import static de.featjar.base.computation.Computations.*;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.*;
import de.featjar.base.computation.*;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.graphviz.GraphVizComputationTreeFormat;
import de.featjar.cli.IFormulaCommand;
import de.featjar.formula.analysis.value.ValueAssignment;
import de.featjar.formula.analysis.value.ValueClause;
import de.featjar.formula.analysis.value.ValueClauseList;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.io.value.ValueAssignmentFormat;
import de.featjar.formula.io.value.ValueAssignmentListFormat;
import de.featjar.formula.structure.formula.IFormula;

import java.time.Duration;
import java.util.List;

/**
 * Computes an analysis result for a formula.
 *
 * @param <T> the type of the analysis result
 */
public abstract class AAnalysisCommand<T> implements IFormulaCommand {
    public static final Option<ValueAssignment> ASSIGNMENT_OPTION = new Option<>(
                    "--assignment", s -> IO.load(s, new ValueAssignmentFormat<>(ValueAssignment::new)))
            .setDescription("An additional assignment to assume")
            .setDefaultValue(new ValueAssignment());

    public static final Option<ValueClauseList> CLAUSES_OPTION = new Option<>(
                    "--clauses", s -> IO.load(s, new ValueAssignmentListFormat<>(ValueClauseList::new, ValueClause::new)))
            .setDescription("An additional clause list to assume")
            .setDefaultValue(new ValueClauseList());

    public static final Option<Duration> TIMEOUT_OPTION = new Option<>("--timeout",
            Result.mapReturnValue(s -> Duration.ofMillis(Long.parseLong(s))))
            .setDescription("Analysis timeout in milliseconds")
            .setValidator(timeout -> !timeout.isNegative())
            .setDefaultValue(ITimeoutDependency.DEFAULT_TIMEOUT);

    public static final Option<Long> SEED_OPTION = new Option<>("--seed", Result.mapReturnValue(Long::valueOf))
            .setDescription("Seed for pseudorandom number generator")
            .setDefaultValue(IRandomDependency.DEFAULT_RANDOM_SEED);

    public static final Option<Boolean> BROWSE_CACHE_OPTION =
            new Flag("--browse-cache").setDescription("Show cache contents in default browser");

    protected IComputation<IFormula> formula;
    protected ArgumentParser argumentParser;

    @Override
    public List<Option<?>> getOptions() {
        return List.of(INPUT_OPTION, BROWSE_CACHE_OPTION);
    }

    @Override
    public void run(ArgumentParser argumentParser) {
        this.argumentParser = argumentParser;
        String input = INPUT_OPTION.parseFrom(argumentParser).get();
        Boolean browseCache = BROWSE_CACHE_OPTION.parseFrom(argumentParser).get();
        this.formula = async(CommandLineInterface.loadFile(input, FeatJAR.extensionPoint(FormulaFormats.class)));
        IComputation<T> computation = newComputation();
        FeatJAR.log().info("running computation");
        FeatJAR.log().debug(computation.print());
        argumentParser.close();
        final long localTime = System.nanoTime();
        final Result<T> result = computation.parallelComputeResult();
        final long timeNeeded = System.nanoTime() - localTime;
        if (result.isPresent()) {
            FeatJAR.log().info("time needed for computation: " + ((timeNeeded / 1_000_000) / 1000.0) + "s");
            System.out.println(serializeResult(result.get()));
        } else {
            System.err.println("Could not compute result.");
            // System.exit(1); // todo: only do this at the very end of running all commands to signal an error
        }
        if (result.hasProblems()) {
            System.err.println("The following problem(s) occurred:");
            result.getProblems().forEach(System.out::println);
        }
        if (browseCache) FeatJAR.cache().browse(new GraphVizComputationTreeFormat());
        this.argumentParser = null;
    }

    public abstract IComputation<T> newComputation();

    public String serializeResult(T result) {
        return result.toString();
    }
}
