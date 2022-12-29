package de.featjar.cli.analysis;


import de.featjar.base.Feat;
import de.featjar.base.cli.*;
import de.featjar.base.computation.*;
import de.featjar.base.data.Pair;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.graphviz.GraphVizComputationTreeFormat;
import de.featjar.base.io.graphviz.GraphVizTreeFormat;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.analysis.bool.BooleanSolution;
import de.featjar.formula.analysis.bool.ComputeBooleanRepresentationOfFormula;
import de.featjar.formula.analysis.sat4j.AnalyzeGetSolutionSAT4J;
import de.featjar.formula.io.value.ValueAssignmentFormat;
import de.featjar.formula.io.value.ValueClauseListFormat;
import de.featjar.formula.analysis.value.ValueAssignment;
import de.featjar.formula.analysis.value.ValueClauseList;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.structure.formula.IFormula;
import de.featjar.formula.structure.formula.connective.And;
import de.featjar.formula.transformer.TransformCNFFormula;
import de.featjar.formula.transformer.TransformNNFFormula;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.featjar.base.computation.Computations.*;

/**
 * Computes an analysis result for a formula.
 *
 * @param <T> the type of the analysis result
 */
public abstract class AAnalysisCommand<T> implements ICommand {
    public static final Option<String> INPUT_OPTION =
            new StringOption("--input")
                    .setDescription("Path to formula file")
                    .setDefaultValue(CommandLineInterface.STANDARD_INPUT);

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
        String input = INPUT_OPTION.parseFrom(argumentParser);
        Boolean browseCache = BROWSE_CACHE_OPTION.parseFrom(argumentParser);
        this.formula = async(CommandLineInterface.loadFile(input, Feat.extensionPoint(FormulaFormats.class)));
        IComputation<T> computation = newComputation();
        Feat.log().info("running " + computation);
        argumentParser.close();
        final long localTime = System.nanoTime();
        final Result<T> result = computation.getResult();
        computation.getResult();
        final long timeNeeded = System.nanoTime() - localTime;
        if (result.isPresent()) {
            Feat.log().info("time needed for computation: " + ((timeNeeded / 1_000_000) / 1000.0) + "s");
            System.out.println(serializeResult(result.get()));
            if (browseCache)
                Feat.cache().browse(new GraphVizComputationTreeFormat());
        } else {
            System.err.println("Could not compute result.");
            // System.exit(1); // todo: only do this at the very end of running all commands to signal an error
        }
        if (!result.getProblem().isEmpty()) {
            System.err.println("The following problem(s) occurred:");
            System.out.println(result.getProblem().get().print());
        }
        this.argumentParser = null;
    }

    public abstract IComputation<T> newComputation();

    public String serializeResult(T result) {
        return result.toString();
    }
}
