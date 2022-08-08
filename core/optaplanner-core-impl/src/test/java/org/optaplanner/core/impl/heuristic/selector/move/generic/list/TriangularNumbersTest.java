package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TriangularNumbersTest {

    static Stream<Arguments> nthProvider() {
        return Stream.of(
                arguments(0, 0),
                arguments(1, 1),
                arguments(2, 3),
                arguments(3, 6),
                arguments(4, 10),
                arguments(5, 15));
    }

    @ParameterizedTest
    @MethodSource("nthProvider")
    void nthTriangle(int n, int nthTriangularNumber) {
        assertThat(TriangularNumbers.nthTriangle(n)).isEqualTo(nthTriangularNumber);
    }

    @ParameterizedTest
    @MethodSource("nthProvider")
    void triangularRoot(int n, int nthTriangularNumber) {
        assertThat(TriangularNumbers.triangularRoot(nthTriangularNumber)).isEqualTo(n);
    }

    static Stream<Arguments> nonTriangularRoots() {
        return Stream.of(
                arguments(2, 1),
                arguments(4, 2),
                arguments(5, 2),
                arguments(7, 3),
                arguments(8, 3),
                arguments(9, 3));
    }

    @ParameterizedTest
    @MethodSource
    void nonTriangularRoots(int x, int rootFloor) {
        assertThat(TriangularNumbers.triangularRoot(x)).isStrictlyBetween((double) rootFloor, rootFloor + 1.0);
    }
}
