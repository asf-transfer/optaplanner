package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.util.Pair;

import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSets;

public final class KOptUtils {

    private KOptUtils() {
    }

    /**
     * Calculate the disjoint k-cycles for {@link KOptDescriptor#getRemovedEdgeIndexToTourOrder()}. <br />
     * <br />
     * Any permutation can be expressed as combination of k-cycles. A k-cycle is a sequence of
     * unique elements (p_1, p_2, ..., p_k) where
     * <ul>
     * <li>p_1 maps to p_2 in the permutation</li>
     * <li>p_2 maps to p_3 in the permutation</li>
     * <li>p_(k-1) maps to p_k in the permutation</li>
     * <li>p_k maps to p_1 in the permutation</li>
     * <li>In general: p_i maps to p_(i+1) in the permutation</li>
     * </ul>
     * For instance, the permutation
     * <ul>
     * <li>1 -> 2</li>
     * <li>2 -> 3</li>
     * <li>3 -> 1</li>
     * <li>4 -> 5</li>
     * <li>5 -> 4</li>
     * </ul>
     * can be expressed as `(1, 2, 3)(4, 5)`.
     *
     * @return The {@link KOptCycleInfo} corresponding to the permutation described by
     *         {@link KOptDescriptor#getRemovedEdgeIndexToTourOrder()}.
     * @param kOptDescriptor The descriptor to calculate cycles for
     */
    static <Solution_, Node_> KOptCycleInfo getCyclesForPermutation(KOptDescriptor<Solution_, Node_> kOptDescriptor) {
        int cycleCount = 0;
        int[] removedEdgeIndexToTourOrder = kOptDescriptor.getRemovedEdgeIndexToTourOrder();
        int[] addedEdgeToOtherEndpoint = kOptDescriptor.getAddedEdgeToOtherEndpoint();
        int[] inverseRemovedEdgeIndexToTourOrder = kOptDescriptor.getInverseRemovedEdgeIndexToTourOrder();

        int[] indexToCycle = new int[removedEdgeIndexToTourOrder.length];
        IntLinkedOpenHashSet remaining = new IntLinkedOpenHashSet(IntSets.fromTo(1, removedEdgeIndexToTourOrder.length));

        while (!remaining.isEmpty()) {
            int currentEndpoint = remaining.firstInt();
            while (remaining.contains(currentEndpoint)) {
                indexToCycle[currentEndpoint] = cycleCount;
                remaining.remove(currentEndpoint);

                // Go to the endpoint connected to this one by an added edge
                int currentEndpointTourIndex = removedEdgeIndexToTourOrder[currentEndpoint];
                int nextEndpointTourIndex = addedEdgeToOtherEndpoint[currentEndpointTourIndex];
                currentEndpoint = inverseRemovedEdgeIndexToTourOrder[nextEndpointTourIndex];

                indexToCycle[currentEndpoint] = cycleCount;
                remaining.remove(currentEndpoint);

                // Go to the endpoint after the added edge
                currentEndpoint = currentEndpoint ^ 1;
            }
            cycleCount++;
        }
        return new KOptCycleInfo(cycleCount, indexToCycle);
    }

    static <Solution_, Node_> List<Pair<Node_, Node_>> getAddedEdgeList(KOptDescriptor<Solution_, Node_> kOptDescriptor) {
        int k = kOptDescriptor.getK();
        List<Pair<Node_, Node_>> out = new ArrayList<>(2 * k);
        int currentEndpoint = 2 * k;

        Node_[] removedEdges = kOptDescriptor.getRemovedEdges();
        int[] addedEdgeToOtherEndpoint = kOptDescriptor.getAddedEdgeToOtherEndpoint();
        int[] removedEdgeIndexToTourOrder = kOptDescriptor.getRemovedEdgeIndexToTourOrder();
        int[] inverseRemovedEdgeIndexToTourOrder = kOptDescriptor.getInverseRemovedEdgeIndexToTourOrder();

        // This loop iterates through the new tour created
        while (currentEndpoint != 0) {
            out.add(Pair.of(removedEdges[currentEndpoint], removedEdges[addedEdgeToOtherEndpoint[currentEndpoint]]));
            int tourIndex = removedEdgeIndexToTourOrder[currentEndpoint];
            int nextEndpointTourIndex = addedEdgeToOtherEndpoint[tourIndex];
            currentEndpoint = inverseRemovedEdgeIndexToTourOrder[nextEndpointTourIndex] ^ 1;
        }
        return out;
    }

    static <Solution_, Node_> List<Pair<Node_, Node_>> getRemovedEdgeList(KOptDescriptor<Solution_, Node_> kOptDescriptor) {
        int k = kOptDescriptor.getK();
        Node_[] removedEdges = kOptDescriptor.getRemovedEdges();
        List<Pair<Node_, Node_>> out = new ArrayList<>(2 * k);
        for (int i = 1; i <= k; i++) {
            out.add(Pair.of(removedEdges[2 * i - 1], removedEdges[2 * i]));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public static <Solution_, Node_> Function<Node_, Node_> getSuccessorFunction(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply,
            IndexVariableSupply indexVariableSupply) {
        return (node) -> {
            List<Node_> valueList =
                    (List<Node_>) listVariableDescriptor.getListVariable(inverseVariableSupply.getInverseSingleton(node));
            int index = indexVariableSupply.getIndex(node);
            if (index == valueList.size() - 1) {
                return valueList.get(0);
            } else {
                return valueList.get(index + 1);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <Solution_, Node_> Function<Node_, Node_> getPredecessorFunction(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply,
            IndexVariableSupply indexVariableSupply) {
        return (node) -> {
            List<Node_> valueList =
                    (List<Node_>) listVariableDescriptor.getListVariable(inverseVariableSupply.getInverseSingleton(node));
            int index = indexVariableSupply.getIndex(node);
            if (index == 0) {
                return valueList.get(valueList.size() - 1);
            } else {
                return valueList.get(index - 1);
            }
        };
    }

    public static <Node_> TriPredicate<Node_, Node_, Node_> getBetweenPredicate(IndexVariableSupply indexVariableSupply) {
        return (start, middle, end) -> {
            int startIndex = indexVariableSupply.getIndex(start);
            int middleIndex = indexVariableSupply.getIndex(middle);
            int endIndex = indexVariableSupply.getIndex(end);

            if (startIndex <= endIndex) {
                // test middleIndex in [startIndex, endIndex]
                return startIndex <= middleIndex && middleIndex <= endIndex;
            } else {
                // test middleIndex in [0, endIndex] or middleIndex in [startIndex, listSize)
                return middleIndex >= startIndex || middleIndex <= endIndex;
            }
        };
    }
}
