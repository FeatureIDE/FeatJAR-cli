package de.featjar.cli.analysis;

import de.featjar.base.cli.Command;
import de.featjar.base.cli.Option;
import de.featjar.base.data.Computation;
import de.featjar.formula.analysis.VariableMap;
import de.featjar.formula.analysis.bool.BooleanAssignment;
import de.featjar.formula.analysis.bool.ComputeBooleanRepresentation;
import de.featjar.formula.analysis.sat4j.SAT4JAnalysis;
import de.featjar.formula.transformer.ComputeCNFFormula;

import java.util.List;

import static de.featjar.base.data.Computations.*;
import static de.featjar.base.data.Computations.async;

public abstract class SAT4JAnalysisCommand<T, U> extends AnalysisCommand<T> {
    @Override
    public List<Option<?>> getOptions() {
        return Command.addOptions(super.getOptions(), ASSIGNMENT_OPTION, CLAUSES_OPTION, TIMEOUT_OPTION);
    }

    @Override
    public Computation<T> newComputation() {
        var booleanRepresentation =
                async(formula)
                        .map(ComputeCNFFormula::new)
                        .map(ComputeBooleanRepresentation.OfFormula::new);
        var booleanClauseList = getKey(booleanRepresentation);
        var variableMap = getValue(booleanRepresentation);
        var result =
                newAnalysis()
                        .setInput(booleanClauseList)
                        .setAssumedAssignment((Computation<BooleanAssignment>) ASSIGNMENT_OPTION.parseFrom(argumentParser).toBoolean(variableMap)) // todo: eliminate cast?
                        .setAssumedClauseList(CLAUSES_OPTION.parseFrom(argumentParser).toBoolean(variableMap))
                        .setTimeout(TIMEOUT_OPTION.parseFrom(argumentParser));
        return interpretResult(result, variableMap);
    }

    public abstract SAT4JAnalysis<?, U> newAnalysis();

    public abstract Computation<T> interpretResult(Computation<U> result, Computation<VariableMap> variableMap);
}
