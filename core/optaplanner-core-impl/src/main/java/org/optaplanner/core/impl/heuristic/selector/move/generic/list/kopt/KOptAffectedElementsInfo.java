package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;

public class KOptAffectedElementsInfo {
    private final int wrappedStartIndex;
    private final int wrappedEndIndex;
    private final List<IntIntImmutablePair> affectedMiddleRangeList;

    private KOptAffectedElementsInfo(int wrappedStartIndex, int wrappedEndIndex,
            List<IntIntImmutablePair> affectedMiddleRangeList) {
        this.wrappedStartIndex = wrappedStartIndex;
        this.wrappedEndIndex = wrappedEndIndex;
        this.affectedMiddleRangeList = affectedMiddleRangeList;
    }

    static KOptAffectedElementsInfo forMiddleRange(int startInclusive, int endExclusive) {
        return new KOptAffectedElementsInfo(-1, -1, List.of(IntIntImmutablePair.of(startInclusive, endExclusive)));
    }

    static KOptAffectedElementsInfo forWrappedRange(int startInclusive, int endExclusive) {
        return new KOptAffectedElementsInfo(startInclusive, endExclusive, List.of());
    }

    // ***********************************************
    // Simple getters
    // ***********************************************

    public int getWrappedStartIndex() {
        return wrappedStartIndex;
    }

    public int getWrappedEndIndex() {
        return wrappedEndIndex;
    }

    public List<IntIntImmutablePair> getAffectedMiddleRangeList() {
        return affectedMiddleRangeList;
    }

    // ***********************************************
    // Complex methods
    // ***********************************************
    public KOptAffectedElementsInfo merge(KOptAffectedElementsInfo other) {
        int newWrappedStartIndex = this.wrappedStartIndex;
        int newWrappedEndIndex = this.wrappedEndIndex;

        if (other.wrappedStartIndex != -1) {
            if (newWrappedStartIndex != -1) {
                newWrappedStartIndex = Math.min(other.wrappedStartIndex, newWrappedStartIndex);
                newWrappedEndIndex = Math.max(other.wrappedEndIndex, newWrappedEndIndex);
            } else {
                newWrappedStartIndex = other.wrappedStartIndex;
                newWrappedEndIndex = other.wrappedEndIndex;
            }
        }

        List<IntIntImmutablePair> newAffectedMiddleRangeList =
                new ArrayList<>(affectedMiddleRangeList.size() + other.affectedMiddleRangeList.size());
        newAffectedMiddleRangeList.addAll(affectedMiddleRangeList);
        newAffectedMiddleRangeList.addAll(other.affectedMiddleRangeList);

        boolean removedAny;
        SearchForIntersectingInterval: do {
            removedAny = false;
            final int listSize = newAffectedMiddleRangeList.size();
            for (int i = 0; i < listSize; i++) {
                for (int j = i + 1; j < listSize; j++) {
                    IntIntImmutablePair leftInterval = newAffectedMiddleRangeList.get(i);
                    IntIntImmutablePair rightInterval = newAffectedMiddleRangeList.get(j);

                    if (leftInterval.leftInt() <= rightInterval.rightInt() &&
                            rightInterval.leftInt() <= leftInterval.rightInt()) {
                        IntIntImmutablePair mergedInterval =
                                new IntIntImmutablePair(Math.min(leftInterval.leftInt(), rightInterval.leftInt()),
                                        Math.max(leftInterval.rightInt(), rightInterval.rightInt()));
                        newAffectedMiddleRangeList.set(i, mergedInterval);
                        newAffectedMiddleRangeList.remove(j);
                        removedAny = true;
                        continue SearchForIntersectingInterval;
                    }
                }
            }
        } while (removedAny);

        return new KOptAffectedElementsInfo(newWrappedStartIndex, newWrappedEndIndex, newAffectedMiddleRangeList);
    }

    @Override
    public String toString() {
        return "KOptAffectedElementsInfo{" +
                "wrappedStartIndex=" + wrappedStartIndex +
                ", wrappedEndIndex=" + wrappedEndIndex +
                ", affectedMiddleRangeList=" + affectedMiddleRangeList +
                '}';
    }
}
