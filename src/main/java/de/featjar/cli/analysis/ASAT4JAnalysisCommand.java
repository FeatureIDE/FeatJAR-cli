package de.featjar.cli.analysis;

import de.featjar.base.cli.ICommand;
import de.featjar.base.cli.Option;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.analysis.VariableMap;
import de.featjar.formula.analysis.bool.BooleanAssignment;
import de.featjar.formula.analysis.bool.BooleanClauseList;
import de.featjar.formula.analysis.bool.AComputeBooleanRepresentation;
import de.featjar.formula.analysis.sat4j.ASAT4JAnalysis;
import de.featjar.formula.transformer.TransformCNFFormula;
import de.featjar.formula.transformer.TransformNNFFormula;

import java.util.List;

import static de.featjar.base.computation.Computations.*;
import static de.featjar.base.computation.Computations.async;

public abstract class ASAT4JAnalysisCommand<T, U> extends AAnalysisCommand<T> {
    @Override
    public List<Option<?>> getOptions() {
        return ICommand.addOptions(super.getOptions(), ASSIGNMENT_OPTION, CLAUSES_OPTION, TIMEOUT_OPTION);
    }

    @Override
    public IComputation<T> newComputation() {
        var booleanRepresentation =
                async(formula)
                        .map(TransformNNFFormula::new)
                        .map(TransformCNFFormula::new)
                        .map(AComputeBooleanRepresentation.OfFormula::new);
        var booleanClauseList = getKey(booleanRepresentation);
        var variableMap = getValue(booleanRepresentation);
        var analysis = newAnalysis(booleanClauseList);
        analysis.setAssumedAssignment((IComputation<BooleanAssignment>) ASSIGNMENT_OPTION.parseFrom(argumentParser).toBoolean(variableMap)); // todo: eliminate cast?
        analysis.setAssumedClauseList(CLAUSES_OPTION.parseFrom(argumentParser).toBoolean(variableMap));
        analysis.setTimeout(async(TIMEOUT_OPTION.parseFrom(argumentParser)));
        return interpret(analysis, variableMap);
    }

    public abstract ASAT4JAnalysis<U> newAnalysis(IComputation<BooleanClauseList> clauseList);

    public abstract IComputation<T> interpret(IComputation<U> result, IComputation<VariableMap> variableMap);
}
