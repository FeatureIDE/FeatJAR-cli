package de.featjar.cli.analysis;


import de.featjar.base.Feat;
import de.featjar.base.cli.CLIArgumentParser;
import de.featjar.base.cli.Command;
import de.featjar.base.cli.CommandLineInterface;
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
            // TODO: currently this is not shown for some reason
            System.err.println("Could not compute result for analysis.");
            if (result.isPresent() && !result.getProblems().isEmpty()) {
                System.err.println("The following problem(s) occurred:");
                result.getProblems().forEach(System.err::println);
            }
        }
        this.argumentParser = null;
    }

    protected abstract Analysis<?, T> newAnalysis();

    protected abstract String serializeResult(T result);
}
