package org.optaplanner.examples.common.swingui.timetable;

public class TimeTableLayoutConstraints {

    private int x;
    private int y;
    private int width;
    private int height;
    private boolean fillCollisions;

    public TimeTableLayoutConstraints(int x, int y) {
        this(x, y, 1, 1, false);
    }

    public TimeTableLayoutConstraints(int x, int y, int width, int height) {
        this(x, y, width, height, false);
    }

    public TimeTableLayoutConstraints(int x, int y, boolean fillCollisions) {
        this(x, y, 1, 1, fillCollisions);
    }

    public TimeTableLayoutConstraints(int x, int y, int width, int height, boolean fillCollisions) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fillCollisions = fillCollisions;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isFillCollisions() {
        return fillCollisions;
    }

    public int getXEnd() {
        return x + width;
    }

    public int getYEnd() {
        return y + height;
    }

}
