package de.featjar.cli.analysis;


import de.featjar.base.FeatJAR;
import de.featjar.base.cli.*;
import de.featjar.base.computation.*;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.graphviz.GraphVizComputationTreeFormat;
import de.featjar.cli.IFormulaCommand;
import de.featjar.formula.io.value.ValueAssignmentFormat;
import de.featjar.formula.io.value.ValueClauseListFormat;
import de.featjar.formula.analysis.value.ValueAssignment;
import de.featjar.formula.analysis.value.ValueClauseList;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.structure.formula.IFormula;

import java.time.Duration;
import java.util.List;

import static de.featjar.base.computation.Computations.*;

/**
 * Computes an analysis result for a formula.
 *
 * @param <T> the type of the analysis result
 */
public abstract class AAnalysisCommand<T> implements IFormulaCommand {
    public static final Option<ValueAssignment> ASSIGNMENT_OPTION =
            new Option<>("--assignment", s -> IO.load(s, new ValueAssignmentFormat()))
                    .setDescription("An additional assignment to assume")
                    .setDefaultValue(new ValueAssignment());

    public static final Option<ValueClauseList> CLAUSES_OPTION =
            new Option<>("--clauses", s -> IO.load(s, new ValueClauseListFormat()))
                    .setDescription("An additional clause list to assume")
                    .setDefaultValue(new ValueClauseList());

    public static final Option<Long> TIMEOUT_OPTION =
            new Option<>("--timeout", Result.mapReturnValue(Long::valueOf))
                    .setDescription("Analysis timeout in milliseconds")
                    .setValidator(timeout -> timeout >= -1)
                    .setDefaultValue(ITimeoutDependency.DEFAULT_TIMEOUT);

    public static final Option<Long> SEED_OPTION =
            new Option<>("--seed", Result.mapReturnValue(Long::valueOf))
                    .setDescription("Seed for pseudorandom number generator")
                    .setDefaultValue(IRandomDependency.DEFAULT_RANDOM_SEED);

    public static final Option<Boolean> BROWSE_CACHE_OPTION =
            new Flag("--browse-cache")
                    .setDescription("Show cache contents in default browser");

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
        if (!result.getProblem().isEmpty()) {
            System.err.println("The following problem(s) occurred:");
            System.out.println(result.getProblem().get().print());
        }
        if (browseCache)
            FeatJAR.cache().browse(new GraphVizComputationTreeFormat());
        this.argumentParser = null;
    }

    public abstract IComputation<T> newComputation();

    public String serializeResult(T result) {
        return result.toString();
    }
}
