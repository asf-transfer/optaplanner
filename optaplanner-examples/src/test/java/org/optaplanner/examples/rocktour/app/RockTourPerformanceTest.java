package org.optaplanner.examples.rocktour.app;

import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.rocktour.domain.RockTourSolution;

class RockTourPerformanceTest extends SolverPerformanceTest<RockTourSolution, HardMediumSoftLongScore> {

    private static final String UNSOLVED_DATA_FILE = "data/rocktour/unsolved/47shows.xlsx";

    @Override
    protected RockTourApp createCommonApp() {
        return new RockTourApp();
    }

    @Override
    protected Stream<TestData<HardMediumSoftLongScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE,
                        HardMediumSoftLongScore.of(0, 72725471, -5212395),
                        HardMediumSoftLongScore.of(0, 72725194, -6558488)),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE,
                        HardMediumSoftLongScore.of(0, 72727353, -4639781),
                        HardMediumSoftLongScore.of(0, 72725627, -5750847)));
    }
}
