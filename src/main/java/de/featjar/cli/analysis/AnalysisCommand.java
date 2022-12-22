package de.featjar.cli.analysis;


import de.featjar.base.Feat;
import de.featjar.base.cli.*;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.formula.io.value.ValueAssignmentFormat;
import de.featjar.formula.io.value.ValueClauseListFormat;
import de.featjar.formula.analysis.value.ValueAssignment;
import de.featjar.formula.analysis.value.ValueClauseList;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.structure.formula.Formula;

import java.util.List;

import static de.featjar.base.computation.Computations.*;

/**
 * Computes an analysis result for a formula.
 *
 * @param <T> the type of the analysis result
 */
public abstract class AnalysisCommand<T> implements ICommand {
    public static final Option<String> INPUT_OPTION =
            new Option.StringOption("--input")
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
            new Option<>("--timeout", Result.wrapInResult(Long::valueOf))
                    .setDescription("Analysis timeout in milliseconds")
                    .setValidator(timeout -> timeout >= 0);

    public static final Option<Long> SEED_OPTION =
            new Option<>("--seed", Result.wrapInResult(Long::valueOf))
                    .setDescription("Seed for pseudorandom number generator")
                    .setDefaultValue(IComputation.WithRandom.DEFAULT_RANDOM_SEED);

    protected IComputation<Formula> formula;
    protected ArgumentParser argumentParser;

    @Override
    public List<Option<?>> getOptions() {
        return List.of(INPUT_OPTION);
    }

    @Override
    public void run(ArgumentParser argumentParser) {
        this.argumentParser = argumentParser;
        String input = INPUT_OPTION.parseFrom(argumentParser);
        this.formula = async(CommandLineInterface.loadFile(input, Feat.extensionPoint(FormulaFormats.class)));
        IComputation<T> computation = newComputation();
        Feat.log().debug("running " + computation);
        argumentParser.close();
        final long localTime = System.nanoTime();
        final Result<T> result = computation.getResult();
        final long timeNeeded = System.nanoTime() - localTime;
        if (result.isPresent() && result.isPresent()) {
            Feat.log().info("time needed for computation: " + ((timeNeeded / 1_000_000) / 1000.0) + "s");
            System.out.println(serializeResult(result.get()));
        } else {
            System.err.println("Could not compute result.");
            if (result.isPresent() && !result.getProblems().isEmpty()) {
                System.err.println("The following problem(s) occurred:");
                result.getProblems().forEach(System.err::println);
            }
            System.exit(1);
        }
        this.argumentParser = null;
    }

    public abstract IComputation<T> newComputation();

    public String serializeResult(T result) {
        return result.toString();
    }
}
