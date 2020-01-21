package sa.dataflow.solver;

import sa.dataflow.analysis.DataFlowAnalysis;
import soot.toolkits.graph.DirectedGraph;

import java.util.LinkedHashMap;

class IterativeSolver<Domain, Node> extends Solver<Domain, Node> {

    IterativeSolver(DataFlowAnalysis<Domain, Node> problem,
                    DirectedGraph<Node> cfg) {
        super(problem, cfg);
        inFlow = new LinkedHashMap<>();
        outFlow = new LinkedHashMap<>();
    }

    @Override
    protected void solveFixedPoint(DirectedGraph<Node> cfg) {
        boolean changed;
        do {
            changed = false;
            for (Node node : cfg) {
                Domain in;
                if (cfg.getHeads().contains(node)) {
                    // In-flow values of head nodes are pre-computed
                    in = inFlow.get(node);
                } else {
                    // Compute and store in-flow values for non-head nodes
                    in = cfg.getPredsOf(node)
                            .stream()
                            .map(outFlow::get)
                            .reduce(problem.newInitialValue(), problem::meet);
                    inFlow.put(node, in);
                }
                Domain out = outFlow.get(node);
                changed |= problem.transfer(in, node, out);
            }
        } while (changed);
    }
}
