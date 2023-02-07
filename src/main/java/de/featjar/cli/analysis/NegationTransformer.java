package de.featjar.cli.analysis;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.Commands;
import de.featjar.base.computation.*;
import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;
import de.featjar.formula.analysis.bool.ComputeBooleanRepresentationOfCNFFormula;
import de.featjar.formula.analysis.sat4j.ComputeSolutionSAT4J;
import de.featjar.formula.analysis.sharpsat.ComputeSolutionCountSharpSAT;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.structure.ExpressionKind;
import de.featjar.formula.structure.formula.FormulaNormalForm;
import de.featjar.formula.structure.formula.IFormula;
import de.featjar.formula.structure.formula.connective.And;
import de.featjar.formula.structure.formula.connective.Not;
import de.featjar.formula.structure.formula.connective.Or;
import de.featjar.formula.structure.formula.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import de.featjar.formula.tester.NormalForms;
import de.featjar.formula.transformer.ComputeCNFFormula;
import de.featjar.formula.transformer.ComputeNNFFormula;
import de.featjar.formula.transformer.DistributiveTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static de.featjar.base.computation.Computations.async;

/**
 * Naive implementation of a transformation into negation-CNF.
 *
 * @author Elias Kuiter
 */
public class NegationTransformer extends AComputation<IFormula> implements ITransformation<IFormula> {
    public static void main(String[] args) {
        FeatJAR.run(featJAR -> {
            var x = async(Commands.loadFile("myb.xml", FeatJAR.extensionPoint(FormulaFormats.class)))
                    .map(ComputeNNFFormula::new)
                    .map(ComputeCNFFormula::new)
                    .map(ComputeBooleanRepresentationOfCNFFormula::new)
                    .peekResult(NegationTransformer.class, "aoeu", c -> System.out.println(c.getKey().stream().count()))
                    .get()
                    .get()
                    .getKey();

            x.addAll(async(Commands.loadFile("myb.xml", FeatJAR.extensionPoint(FormulaFormats.class)))
                    .map(ComputeNNFFormula::new)
                    //.map(ComputeCNFFormula::new)
                    .map(NegationTransformer::new)
                    .map(ComputeBooleanRepresentationOfCNFFormula::new)
                    .peekResult(NegationTransformer.class, "aoeu", c -> System.out.println(c.getKey().stream().count()))
                    .get()
                    .get()
                    .getKey());

            async(Commands.loadFile("myb.xml", FeatJAR.extensionPoint(FormulaFormats.class)))
                    .map(ComputeNNFFormula::new)
                    .map(ComputeCNFFormula::new)
                    .map(ComputeSolutionCountSharpSAT::new)
                    .peekResult(NegationTransformer.class, "aoeu", c -> System.out.println(c))
                    .get();

            async(Commands.loadFile("myb.xml", FeatJAR.extensionPoint(FormulaFormats.class)))
                    .map(ComputeNNFFormula::new)
                    //.map(ComputeCNFFormula::new)
                    .map(NegationTransformer::new)
                    .map(ComputeSolutionCountSharpSAT::new)
                    .peekResult(NegationTransformer.class, "aoeu", c -> System.out.println(c))
                    .get();


            System.out.println(new ComputeSolutionSAT4J(async(x)).get());
        });
    }

    protected static final Dependency<IFormula> NNF_FORMULA = newRequiredDependency();

    public NegationTransformer(IComputation<IFormula> nnfFormula) {
        dependOn(NNF_FORMULA);
        setInput(nnfFormula);
    }

    @Override
    public Dependency<IFormula> getInputDependency() {
        return NNF_FORMULA;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<IFormula> compute(DependencyList dependencyList, Progress progress) {
        IFormula nnfFormula = dependencyList.get(NNF_FORMULA);
        ExpressionKind.NNF.assertFor(nnfFormula);
        AtomicInteger i = new AtomicInteger();

        List<IFormula> clauseFormulas = new ArrayList<>();
        Consumer<IFormula> transformer = formula -> {
            transform(formula, clauseFormulas, i.incrementAndGet());
            progress.incrementCurrentStep();
        };

        if (nnfFormula instanceof And) {
            List<IFormula> children = (List<IFormula>) nnfFormula.getChildren();
            progress.setTotalSteps(children.size());
            children.forEach(transformer);
        } else {
            throw new RuntimeException("expected proto-CNF");
        }

        Literal rootpos = new Literal(true, new Variable("__my__root"));
        Literal rootneg = new Literal(false, new Variable("__my__root"));

        IFormula f = new Or();
        f.addChild(rootpos);
        for (int j = 1; j <= i.get(); j++) {
            clauseFormulas.add(new Or(rootneg, new Literal(new Variable("__my__" + j))));
            f.addChild(new Literal(false, new Variable("__my__" + j)));
        }
        clauseFormulas.add(f);

        clauseFormulas.add(rootpos);
        //clauseFormulas.add(rootneg);

        return Result.of(NormalForms.normalToStrictNormalForm(new And(clauseFormulas), FormulaNormalForm.CNF));
    }

    @SuppressWarnings("unchecked")
    private void transform(IFormula formula, List<IFormula> clauseFormulas, int i) {
        Literal litpos = new Literal(true, new Variable("__my__" + i));
        Literal litneg = new Literal(false, new Variable("__my__" + i));
        Result<IFormula> transformationResult = distributiveTransform(formula);
        if (transformationResult.isPresent()) {
            List<? extends IFormula> cnfClauses = (List<? extends IFormula>) transformationResult.get().getChildren();
            for (IFormula cnfClause : cnfClauses) {
                Or cnfClause2 = new Or((List<? extends IFormula>) (Object) cnfClause.getChildren().stream().map(c -> c.getClass().equals(Variable.class) ? new Literal((Variable) c) : c).collect(Collectors.toList()));
                cnfClause2.addChild(litneg);
                clauseFormulas.add(cnfClause2);
            }
        } else
            throw new RuntimeException("failed to transform subformula");
        transformationResult = distributiveTransform(new ComputeNNFFormula(Computations.async(new Not(formula))).get().get());
        if (transformationResult.isPresent()) {
            List<? extends IFormula> cnfClauses = (List<? extends IFormula>) transformationResult.get().getChildren();
            for (IFormula cnfClause : cnfClauses) {
                Or cnfClause2 = new Or((List<? extends IFormula>) (Object) cnfClause.getChildren().stream().map(c -> c.getClass().equals(Variable.class) ? new Literal((Variable) c) : c).collect(Collectors.toList()));
                cnfClause2.addChild(litpos);
                clauseFormulas.add(cnfClause2);
            }
        } else
            throw new RuntimeException("failed to transform subformula");
    }

    protected Result<IFormula> distributiveTransform(IFormula formula) {
        return new DistributiveTransformer(true).apply(formula);
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new ComputeCNFFormula(getInput());
    }
}
