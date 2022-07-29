package org.optaplanner.constraint.streams.bavet.common.index;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import org.optaplanner.constraint.streams.bavet.common.Tuple;
import org.optaplanner.core.impl.score.stream.JoinerType;

final class ComparisonIndexer<Tuple_ extends Tuple, Value_, Key_ extends Comparable<Key_>>
        implements Indexer<Tuple_, Value_> {

    private final Function<IndexProperties, Key_> indexKeyFunction;
    private final Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier;
    private final BiPredicate<Key_, Key_> iterationStoppingCondition;
    private final SortedMap<Key_, Indexer<Tuple_, Value_>> comparisonMap;

    public ComparisonIndexer(JoinerType comparisonJoinerType, Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier) {
        this(comparisonJoinerType, 0, downstreamIndexerSupplier);
    }

    public ComparisonIndexer(JoinerType comparisonJoinerType, int indexKeyPosition,
            Supplier<Indexer<Tuple_, Value_>> downstreamIndexerSupplier) {
        this.indexKeyFunction = indexProperties -> indexProperties.getProperty(indexKeyPosition);
        this.downstreamIndexerSupplier = Objects.requireNonNull(downstreamIndexerSupplier);
        this.iterationStoppingCondition = getIterationStoppingCondition(comparisonJoinerType);
        /*
         * For GT/GTE, the iteration order is reversed.
         * This allows us to iterate over the entire map, stopping when the given condition is reached.
         * This is done so that we can avoid using head/tail sub maps, which are expensive.
         */
        Comparator<Key_> keyComparator =
                (comparisonJoinerType == JoinerType.GREATER_THAN || comparisonJoinerType == JoinerType.GREATER_THAN_OR_EQUAL)
                        ? KeyComparator.COMPARATOR_REVERSED
                        : KeyComparator.COMPARATOR;
        this.comparisonMap = new TreeMap<>(keyComparator);
    }

    private static <Key_ extends Comparable<Key_>> BiPredicate<Key_, Key_>
            getIterationStoppingCondition(JoinerType comparisonJoinerType) {
        switch (comparisonJoinerType) {
            case LESS_THAN:
                return (currentKey, stopKey) -> currentKey.compareTo(stopKey) >= 0;
            case LESS_THAN_OR_EQUAL:
                return (currentKey, stopKey) -> currentKey.compareTo(stopKey) > 0;
            case GREATER_THAN:
                return (currentKey, stopKey) -> currentKey.compareTo(stopKey) <= 0;
            case GREATER_THAN_OR_EQUAL:
                return (currentKey, stopKey) -> currentKey.compareTo(stopKey) < 0;
            default:
                throw new IllegalStateException("Impossible state: the comparisonJoinerType (" + comparisonJoinerType
                        + ") is not one of the 4 comparison types.");
        }
    }

    @Override
    public void visit(IndexProperties indexProperties, BiConsumer<Tuple_, Value_> tupleValueVisitor) {
        Key_ indexKey = indexKeyFunction.apply(indexProperties);
        for (Map.Entry<Key_, Indexer<Tuple_, Value_>> entry : comparisonMap.entrySet()) {
            Key_ key = entry.getKey();
            if (iterationStoppingCondition.test(key, indexKey)) {
                return;
            }
            Indexer<Tuple_, Value_> indexer = entry.getValue();
            indexer.visit(indexProperties, tupleValueVisitor);
        }
    }

    @Override
    public Value_ get(IndexProperties indexProperties, Tuple_ tuple) {
        Key_ indexKey = indexKeyFunction.apply(indexProperties);
        Indexer<Tuple_, Value_> downstreamIndexer = getDownstreamIndexer(indexProperties, indexKey, tuple);
        return downstreamIndexer.get(indexProperties, tuple);
    }

    @Override
    public void put(IndexProperties indexProperties, Tuple_ tuple, Value_ value) {
        Key_ indexKey = indexKeyFunction.apply(indexProperties);
        // Avoids computeIfAbsent in order to not create lambdas on the hot path.
        Indexer<Tuple_, Value_> downstreamIndexer = comparisonMap.get(indexKey);
        if (downstreamIndexer == null) {
            downstreamIndexer = downstreamIndexerSupplier.get();
            comparisonMap.put(indexKey, downstreamIndexer);
        }
        downstreamIndexer.put(indexProperties, tuple, value);
    }

    @Override
    public Value_ remove(IndexProperties indexProperties, Tuple_ tuple) {
        Key_ indexKey = indexKeyFunction.apply(indexProperties);
        Indexer<Tuple_, Value_> downstreamIndexer = getDownstreamIndexer(indexProperties, indexKey, tuple);
        Value_ value = downstreamIndexer.remove(indexProperties, tuple);
        if (downstreamIndexer.isEmpty()) {
            comparisonMap.remove(indexKey);
        }
        return value;
    }

    private Indexer<Tuple_, Value_> getDownstreamIndexer(IndexProperties indexProperties, Key_ indexerKey, Tuple_ tuple) {
        Indexer<Tuple_, Value_> downstreamIndexer = comparisonMap.get(indexerKey);
        if (downstreamIndexer == null) {
            throw new IllegalStateException("Impossible state: the tuple (" + tuple
                    + ") with indexProperties (" + indexProperties
                    + ") doesn't exist in the indexer " + this + ".");
        }
        return downstreamIndexer;
    }

    @Override
    public boolean isEmpty() {
        return comparisonMap.isEmpty();
    }

    private static final class KeyComparator<Key_ extends Comparable<Key_>> implements Comparator<Key_> {

        private static final Comparator COMPARATOR = new KeyComparator<>();
        private static final Comparator COMPARATOR_REVERSED = COMPARATOR.reversed();

        @Override
        public int compare(Key_ o1, Key_ o2) { // Exists so that the comparison operations can be more easily debugged.
            return o1.compareTo(o2);
        }

    }

}