package de.featjar.cli.analysis;


import de.featjar.base.Feat;
import de.featjar.base.cli.CLIArgumentParser;
import de.featjar.base.cli.Command;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.formula.analysis.Analysis;
import de.featjar.formula.analysis.io.ValueAssignmentFormat;
import de.featjar.formula.analysis.io.ValueClauseListFormat;
import de.featjar.formula.analysis.value.ValueAssignment;
import de.featjar.formula.analysis.value.ValueClauseList;
import de.featjar.formula.structure.formula.Formula;

/**
 * Analyzes a formula.
 *
 * @param <T> the type of the analysis result
 */
public abstract class AnalysisCommand<T> implements Command {
    protected Formula formula;
    protected CLIArgumentParser argumentParser;

    public void setFormula(Formula formula) {
        this.formula = formula;
    }

    protected Long parseTimeout() {
        Long timeout = argumentParser.parseOption("--timeout").map(Long::valueOf).orElse(null);
        if (timeout != null && timeout < 0)
            throw new IllegalArgumentException("negative timeout is invalid");
        return timeout;
    }

    protected ValueAssignment parseValueAssignment() {
        // todo: add documentation for options (or even extract a dedicated class for options)
        return Result.ofOptional(argumentParser.parseOption("--assignment"))
                .flatMap(s -> IO.load(s, new ValueAssignmentFormat()))
                .orElse(new ValueAssignment());
    }

    protected ValueClauseList parseValueClauseList() {
        return Result.ofOptional(argumentParser.parseOption("--clauses"))
                .flatMap(s -> IO.load(s, new ValueClauseListFormat()))
                .orElse(new ValueClauseList());
    }

    protected Long parseSeed() {
        return argumentParser.parseOption("--seed").map(Long::valueOf).orElse(null);
    }

    // TODO: also parse other typical parameters, such as assignment, clause list, and seed

    @Override
    public void run(CLIArgumentParser argumentParser) {
        if (this.formula == null)
            throw new IllegalArgumentException("no formula given");
        this.argumentParser = argumentParser;
        Analysis<?, T> analysis = newAnalysis();
        Feat.log().debug(analysis);
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
