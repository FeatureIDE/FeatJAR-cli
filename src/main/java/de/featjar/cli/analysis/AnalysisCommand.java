package de.featjar.cli.analysis;


import de.featjar.base.Feat;
import de.featjar.base.cli.CLIArgumentParser;
import de.featjar.base.cli.Command;
import de.featjar.base.cli.CommandLine;
import de.featjar.base.data.Result;
import de.featjar.formula.analysis.Analysis;
import de.featjar.formula.structure.formula.Formula;

import java.util.Optional;

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

    // todo: also parse other typical parameters, such as assignment, clause list, and seed

    @Override
    public void run(CLIArgumentParser argumentParser) {
        if (this.formula == null)
            throw new IllegalArgumentException("no formula given");
        this.argumentParser = argumentParser;
        Analysis<?, T> analysis = newAnalysis();
        Feat.log().debug(analysis);
        argumentParser.close();
        final long localTime = System.nanoTime();
        // todo: helper for flattening optional and result
        final Optional<Result<T>> result = CommandLine.runInThread(analysis::getResult, parseTimeout());
        final long timeNeeded = System.nanoTime() - localTime;
        if (result.isPresent() && result.get().isPresent()) {
            System.out.println("Time needed for analysis:");
            System.out.println(((timeNeeded / 1_000_000) / 1000.0) + "s");
            System.out.println("Result for analysis:");
            System.out.println(serializeResult(result.get().get()));
        } else {
            // todo: currently this is not shown for some reason
            System.err.println("Could not compute result for analysis.");
            if (result.isPresent() && !result.get().getProblems().isEmpty()) {
                System.err.println("The following problem(s) occurred:");
                result.get().getProblems().forEach(System.err::println);
            }
        }
        this.argumentParser = null;
    }

    protected abstract Analysis<?, T> newAnalysis();

    protected abstract String serializeResult(T result);
}
