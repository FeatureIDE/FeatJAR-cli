package de.featjar.cli.analysis;


import de.featjar.base.Feat;
import de.featjar.base.cli.ArgumentParser;
import de.featjar.base.cli.CLIArgumentParser;
import de.featjar.base.cli.Command;
import de.featjar.base.cli.CommandLineInterface;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.formula.analysis.Analysis;
import de.featjar.formula.analysis.io.ValueAssignmentFormat;
import de.featjar.formula.analysis.io.ValueClauseListFormat;
import de.featjar.formula.analysis.value.ValueAssignment;
import de.featjar.formula.analysis.value.ValueClauseList;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.structure.formula.Formula;

/**
 * Analyzes a formula.
 *
 * @param <T> the type of the analysis result
 */
public abstract class AnalysisCommand<T> implements Command {
    public static final ArgumentParser.Option<String> INPUT_OPTION =
            new ArgumentParser.StringOption("--input")
                    .setDescription("Path to formula file")
                    .setDefaultValue(CommandLineInterface.STANDARD_INPUT);

    public static final ArgumentParser.Option<ValueAssignment> ASSIGNMENT_OPTION =
            new ArgumentParser.Option<>("--assignment", s -> IO.load(s, new ValueAssignmentFormat()))
                    .setDescription("An additional assignment to assume")
                    .setDefaultValue(new ValueAssignment());

    public static final ArgumentParser.Option<ValueClauseList> CLAUSES_OPTION =
            new ArgumentParser.Option<>("--clauses", s -> IO.load(s, new ValueClauseListFormat()))
                    .setDescription("An additional clause list to assume")
                    .setDefaultValue(new ValueClauseList());
    public static final ArgumentParser.Option<Long> TIMEOUT_OPTION =
            new ArgumentParser.Option<>("--timeout", Result.wrapInResult(Long::valueOf))
                    .setDescription("Analysis timeout in milliseconds")
                    .setValidator(timeout -> timeout >= 0);

    public static final ArgumentParser.Option<Long> SEED_OPTION =
            new ArgumentParser.Option<>("--seed", Result.wrapInResult(Long::valueOf))
                    .setDescription("Seed for pseudorandom number generator")
                    .setDefaultValue(Analysis.WithRandom.DEFAULT_RANDOM_SEED);

    protected Formula formula;
    protected CLIArgumentParser argumentParser;

    @Override
    public void run(CLIArgumentParser argumentParser) {
        this.argumentParser = argumentParser;
        String input = INPUT_OPTION.parseFrom(argumentParser);
        this.formula = CommandLineInterface.loadFile(input, Feat.extensionPoint(FormulaFormats.class)).orElseThrow();
        Analysis<?, T> analysis = newAnalysis();
        Feat.log().debug("running " + analysis);
        argumentParser.close();
        final long localTime = System.nanoTime();
        final Result<T> result = analysis.getResult();
        final long timeNeeded = System.nanoTime() - localTime;
        if (result.isPresent() && result.isPresent()) {
            System.out.println("Time needed for analysis:");
            System.out.println(((timeNeeded / 1_000_000) / 1000.0) + "s");
            System.out.println("Result for analysis:");
            System.out.println(serializeResult(result.get()));
        } else {
            System.err.println("Could not compute result for analysis.");
            if (result.isPresent() && !result.getProblems().isEmpty()) {
                System.err.println("The following problem(s) occurred:");
                result.getProblems().forEach(System.err::println);
            }
            System.exit(1);
        }
        this.argumentParser = null;
    }

    protected abstract Analysis<?, T> newAnalysis();

    protected abstract String serializeResult(T result);
}
