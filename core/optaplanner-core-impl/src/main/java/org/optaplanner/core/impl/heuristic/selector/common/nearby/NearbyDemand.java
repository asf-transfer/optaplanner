package org.optaplanner.core.impl.heuristic.selector.common.nearby;

import java.util.Objects;
import java.util.function.ToIntFunction;

import org.optaplanner.core.impl.domain.variable.supply.Demand;
import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.selector.Selector;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
import org.optaplanner.core.impl.solver.ClassInstanceCache;

/**
 * Calculating {@link NearbyDistanceMatrix} is very expensive,
 * therefore we want to reuse it as much as possible.
 *
 * <p>
 * In cases where the demand represents the same nearby selector (as defined by {@link NearbyDemand#equals(Object)})
 * the {@link SupplyManager} ensures that the same {@link NearbySupply} instance is returned
 * with the pre-computed {@link NearbyDistanceMatrix}.
 *
 * @param <Solution_>
 * @param <Origin_>
 * @param <Destination_>
 */
public final class NearbyDemand<Solution_, Origin_, Destination_>
        implements Demand<NearbySupply<Origin_, Destination_>> {

    private final NearbyDistanceMeter<Origin_, Destination_> meter;
    private final Selector<Solution_> childSelector;
    private final EntitySelector<Solution_> replayingOriginEntitySelector;
    private final ToIntFunction<Origin_> destinationSizeFunction;

    public NearbyDemand(NearbyDistanceMeter<Origin_, Destination_> meter, Selector<Solution_> childSelector,
            EntitySelector<Solution_> replayingOriginEntitySelector, ToIntFunction<Origin_> destinationSizeFunction) {
        this.meter = meter;
        this.childSelector = childSelector;
        this.replayingOriginEntitySelector = replayingOriginEntitySelector;
        this.destinationSizeFunction = destinationSizeFunction;
    }

    @Override
    public NearbySupply<Origin_, Destination_> createExternalizedSupply(SupplyManager supplyManager) {
        long originSize = replayingOriginEntitySelector.getSize();
        if (originSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The originEntitySelector (" + replayingOriginEntitySelector
                    + ") has an entitySize (" + originSize
                    + ") which is higher than Integer.MAX_VALUE.");
        }
        NearbyDistanceMatrix<Origin_, Destination_> nearbyDistanceMatrix = childSelector instanceof EntitySelector
                ? new NearbyDistanceMatrix<>(meter, (int) originSize, (EntitySelector<?>) childSelector,
                        destinationSizeFunction)
                : new NearbyDistanceMatrix<>(meter, (int) originSize, (ValueSelector<?>) childSelector,
                        destinationSizeFunction);
        replayingOriginEntitySelector.endingIterator()
                .forEachRemaining(origin -> nearbyDistanceMatrix.addAllDestinations((Origin_) origin));
        return () -> nearbyDistanceMatrix;
    }

    /**
     * Two instances of this class are consider equal if and only if:
     *
     * <ul>
     * <li>Their meter instances are equal.</li>
     * <li>Their child selectors are equal.</li>
     * <li>Their replaying origin entity selectors are equal.</li>
     * </ul>
     *
     * Otherwise as defined by {@link Object#equals(Object)}.
     *
     * @see ClassInstanceCache for how we ensure equality for meter instances in particular and selectors in general.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NearbyDemand<?, ?, ?> that = (NearbyDemand<?, ?, ?>) o;
        return Objects.equals(meter, that.meter)
                && Objects.equals(childSelector, that.childSelector)
                && Objects.equals(replayingOriginEntitySelector, that.replayingOriginEntitySelector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meter, childSelector, replayingOriginEntitySelector);
    }
}
