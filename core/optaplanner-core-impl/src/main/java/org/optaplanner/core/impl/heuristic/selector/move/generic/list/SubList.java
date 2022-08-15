package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

public final class SubList {

    private final Object entity;
    private final int fromIndex;
    private final int length;

    SubList(Object entity, int fromIndex, int length) {
        this.entity = entity;
        this.fromIndex = fromIndex;
        this.length = length;
    }

    public Object getEntity() {
        return entity;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public int getLength() {
        return length;
    }

    public int getToIndex() {
        return fromIndex + length;
    }

    @Override
    public String toString() {
        return entity + "[" + fromIndex + ".." + getToIndex() + "]";
    }
}
