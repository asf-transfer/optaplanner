package org.optaplanner.examples.projectjobscheduling.app;

import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.projectjobscheduling.domain.Schedule;

class ProjectJobSchedulingPerformanceTest extends SolverPerformanceTest<Schedule, HardMediumSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/projectjobscheduling/unsolved/A-4.xml";

    @Override
    protected ProjectJobSchedulingApp createCommonApp() {
        return new ProjectJobSchedulingApp();
    }

    @Override
    protected Stream<TestData<HardMediumSoftScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE, HardMediumSoftScore.of(0, -345, -145),
                        EnvironmentMode.REPRODUCIBLE),
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE, HardMediumSoftScore.of(0, -771, -316),
                        EnvironmentMode.FAST_ASSERT),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE, HardMediumSoftScore.of(0, -113, -60),
                        EnvironmentMode.REPRODUCIBLE),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE, HardMediumSoftScore.of(0, -138, -65),
                        EnvironmentMode.FAST_ASSERT));
    }
}
