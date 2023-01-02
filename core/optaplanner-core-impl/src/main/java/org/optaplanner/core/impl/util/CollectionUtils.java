package org.optaplanner.core.impl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CollectionUtils {

    /**
     * Creates a copy of the list, optionally in reverse order.
     *
     * @param originalList the list to copy, preferably {@link ArrayList}
     * @param reverse true if the resulting list should have its order reversed
     * @return mutable list, never null
     * @param <E> the type of elements in the list
     */
    public static <E> List<E> copy(List<E> originalList, boolean reverse) {
        if (!reverse) {
            return new ArrayList<>(originalList);
        }
        /*
         * Some move implementations on the hot path rely heavily on list reversal.
         * As such, the following implementation was benchmarked to perform as well as possible for lists of all sizes.
         * See PLANNER-2808 for details.
         */
        switch (originalList.size()) {
            case 0:
                return new ArrayList<>(0);
            case 1:
                List<E> singletonList = new ArrayList<>(1);
                singletonList.add(originalList.get(0));
                return singletonList;
            case 2:
                List<E> smallList = new ArrayList<>(2);
                smallList.add(originalList.get(1));
                smallList.add(originalList.get(0));
                return smallList;
            default:
                List<E> largeList = new ArrayList<>(originalList);
                Collections.reverse(largeList);
                return largeList;

        }
    }

    private CollectionUtils() {
        // No external instances.
    }

}
