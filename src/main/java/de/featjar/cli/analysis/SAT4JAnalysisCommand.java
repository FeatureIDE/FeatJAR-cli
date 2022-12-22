package de.featjar.cli.analysis;

import de.featjar.base.cli.Command;
import de.featjar.base.cli.Option;
import de.featjar.base.computation.Computable;
import de.featjar.formula.analysis.VariableMap;
import de.featjar.formula.analysis.bool.BooleanAssignment;
import de.featjar.formula.analysis.bool.BooleanClauseList;
import de.featjar.formula.analysis.bool.ComputeBooleanRepresentation;
import de.featjar.formula.analysis.sat4j.SAT4JAnalysis;
import de.featjar.formula.transformer.TransformCNFFormula;
import de.featjar.formula.transformer.TransformNNFFormula;

import java.util.List;

import static de.featjar.base.computation.Computations.*;
import static de.featjar.base.computation.Computations.async;

public abstract class SAT4JAnalysisCommand<T, U> extends AnalysisCommand<T> {
    @Override
    public List<Option<?>> getOptions() {
        return Command.addOptions(super.getOptions(), ASSIGNMENT_OPTION, CLAUSES_OPTION, TIMEOUT_OPTION);
    }

    @Override
    public Computable<T> newComputation() {
        var booleanRepresentation =
                async(formula)
                        .map(TransformNNFFormula::new)
                        .map(TransformCNFFormula::new)
                        .map(ComputeBooleanRepresentation.OfFormula::new);
        var booleanClauseList = getKey(booleanRepresentation);
        var variableMap = getValue(booleanRepresentation);
        var analysis = newAnalysis(booleanClauseList);
        analysis.setAssumedAssignment((Computable<BooleanAssignment>) ASSIGNMENT_OPTION.parseFrom(argumentParser).toBoolean(variableMap)); // todo: eliminate cast?
        analysis.setAssumedClauseList(CLAUSES_OPTION.parseFrom(argumentParser).toBoolean(variableMap));
        analysis.setTimeout(async(TIMEOUT_OPTION.parseFrom(argumentParser)));
        return interpret(analysis, variableMap);
    }

    public abstract SAT4JAnalysis<U> newAnalysis(Computable<BooleanClauseList> clauseList);

    public abstract Computable<T> interpret(Computable<U> result, Computable<VariableMap> variableMap);
}
