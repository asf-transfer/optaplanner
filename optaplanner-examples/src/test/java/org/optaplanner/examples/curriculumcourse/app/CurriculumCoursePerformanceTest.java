package org.optaplanner.examples.curriculumcourse.app;

import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.curriculumcourse.domain.CourseSchedule;

class CurriculumCoursePerformanceTest extends SolverPerformanceTest<CourseSchedule, HardSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/curriculumcourse/unsolved/comp01_initialized.xml";

    @Override
    protected CurriculumCourseApp createCommonApp() {
        return new CurriculumCourseApp();
    }

    @Override
    protected Stream<TestData<HardSoftScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE, HardSoftScore.ofSoft(-66),
                        EnvironmentMode.REPRODUCIBLE),
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE, HardSoftScore.ofSoft(-82),
                        EnvironmentMode.FAST_ASSERT),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE, HardSoftScore.ofSoft(-38),
                        EnvironmentMode.REPRODUCIBLE),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE, HardSoftScore.ofSoft(-43),
                        EnvironmentMode.FAST_ASSERT));
    }
}
