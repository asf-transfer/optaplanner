package org.optaplanner.examples.examination.app;

import java.util.stream.Stream;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.examples.common.app.SolverPerformanceTest;
import org.optaplanner.examples.examination.domain.Examination;

class ExaminationPerformanceTest extends SolverPerformanceTest<Examination, HardSoftScore> {

    private static final String UNSOLVED_DATA_FILE = "data/examination/unsolved/exam_comp_set5.xml";

    @Override
    protected ExaminationApp createCommonApp() {
        return new ExaminationApp();
    }

    @Override
    protected Stream<TestData<HardSoftScore>> testData() {
        return Stream.of(
                TestData.of(ConstraintStreamImplType.DROOLS, UNSOLVED_DATA_FILE,
                        HardSoftScore.of(0, -4357),
                        HardSoftScore.of(0, -4361)),
                TestData.of(ConstraintStreamImplType.BAVET, UNSOLVED_DATA_FILE,
                        HardSoftScore.of(0, -4107),
                        HardSoftScore.of(0, -4116)));
    }
}
