package org.optaplanner.examples.common.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.examples.common.TestSystemProperties;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

/**
 * Runs an example {@link Solver}.
 * <p>
 * A test should run in less than 10 seconds on a 3 year old desktop computer, choose the bestScoreLimit accordingly.
 * Always use a {@link Timeout} on {@link Test}, preferably 10 minutes because some of the Jenkins machines are old.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public abstract class SolverPerformanceTest<Solution_, Score_ extends Score<Score_>> extends LoggingTest {

    private static final String MOVE_THREAD_COUNTS_STRING = System.getProperty(TestSystemProperties.MOVE_THREAD_COUNTS);

    protected SolutionFileIO<Solution_> solutionFileIO;
    protected String solverConfigResource;

    private static Stream<String> moveThreadCounts() {
        return Optional.ofNullable(MOVE_THREAD_COUNTS_STRING)
                .map(s -> Arrays.stream(s.split(",")))
                .orElse(Stream.of(SolverConfig.MOVE_THREAD_COUNT_NONE));
    }

    @TestFactory
    @Execution(ExecutionMode.CONCURRENT)
    @Timeout(600)
    Stream<DynamicTest> runSpeedTest() {
        return moveThreadCounts().flatMap(moveThreadCount -> testData().flatMap(testData -> {
            Stream.Builder<DynamicTest> streamBuilder = Stream.builder();
            streamBuilder.add(createDynamicTest(testData.constraintStreamImplType, testData.unsolvedDataFile,
                    EnvironmentMode.REPRODUCIBLE,
                    testData.bestScoreLimitForReproducible, moveThreadCount));
            if (testData.bestScoreLimitForFastAssert != null) {
                streamBuilder.add(createDynamicTest(testData.constraintStreamImplType, testData.unsolvedDataFile,
                        EnvironmentMode.FAST_ASSERT,
                        testData.bestScoreLimitForFastAssert, moveThreadCount));
            }
            if (testData.bestScoreLimitForFullAssert != null) {
                streamBuilder.add(createDynamicTest(testData.constraintStreamImplType, testData.unsolvedDataFile,
                        EnvironmentMode.FULL_ASSERT,
                        testData.bestScoreLimitForFullAssert, moveThreadCount));
            }
            return streamBuilder.build();
        }));
    }

    private DynamicTest createDynamicTest(ConstraintStreamImplType constraintStreamImplType, String unsolvedDataFile,
            EnvironmentMode environmentMode, Score_ bestScoreLimit, String moveThreadCount) {
        String testName = constraintStreamImplType + ", " +
                unsolvedDataFile.replaceFirst(".*/", "")
                + ", "
                + environmentMode
                + ", threads: " + moveThreadCount;
        return dynamicTest(testName,
                () -> runSpeedTest(constraintStreamImplType,
                        new File(unsolvedDataFile),
                        bestScoreLimit,
                        environmentMode,
                        moveThreadCount));
    }

    @BeforeEach
    public void setUp() {
        CommonApp<Solution_> commonApp = createCommonApp();
        solutionFileIO = commonApp.createSolutionFileIO();
        solverConfigResource = commonApp.getSolverConfigResource();
    }

    protected abstract CommonApp<Solution_> createCommonApp();

    protected abstract Stream<TestData<Score_>> testData();

    private void runSpeedTest(ConstraintStreamImplType constraintStreamImplType, File unsolvedDataFile, Score_ bestScoreLimit,
            EnvironmentMode environmentMode, String moveThreadCount) {
        SolverFactory<Solution_> solverFactory =
                buildSolverFactory(constraintStreamImplType, bestScoreLimit, environmentMode, moveThreadCount);
        Solution_ problem = solutionFileIO.read(unsolvedDataFile);
        logger.info("Opened: {}", unsolvedDataFile);
        Solver<Solution_> solver = solverFactory.buildSolver();
        Solution_ bestSolution = solver.solve(problem);
        assertScoreAndConstraintMatches(solverFactory, bestSolution, bestScoreLimit);
    }

    private SolverFactory<Solution_> buildSolverFactory(ConstraintStreamImplType constraintStreamImplType,
            Score_ bestScoreLimit, EnvironmentMode environmentMode, String moveThreadCount) {
        SolverConfig solverConfig = SolverConfig.createFromXmlResource(solverConfigResource);
        solverConfig.withEnvironmentMode(environmentMode)
                .withTerminationConfig(new TerminationConfig()
                        .withBestScoreLimit(bestScoreLimit.toString()))
                .withMoveThreadCount(moveThreadCount);
        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig =
                Objects.requireNonNullElseGet(solverConfig.getScoreDirectorFactoryConfig(),
                        ScoreDirectorFactoryConfig::new);
        if (scoreDirectorFactoryConfig.getConstraintProviderClass() == null) {
            Assertions.fail("Test does not support constraint streams.");
        }
        scoreDirectorFactoryConfig.setConstraintStreamImplType(constraintStreamImplType);
        solverConfig.setScoreDirectorFactoryConfig(scoreDirectorFactoryConfig);
        return SolverFactory.create(solverConfig);
    }

    private void assertScoreAndConstraintMatches(SolverFactory<Solution_> solverFactory, Solution_ bestSolution,
            Score_ bestScoreLimit) {
        assertThat(bestSolution).isNotNull();
        ScoreManager<Solution_, Score_> scoreManager = ScoreManager.create(solverFactory);
        Score_ bestScore = scoreManager.updateScore(bestSolution);
        assertThat(bestScore)
                .as("The bestScore (" + bestScore + ") must be at least the bestScoreLimit (" + bestScoreLimit + ").")
                .isGreaterThanOrEqualTo(bestScoreLimit);

        ScoreExplanation<Solution_, Score_> scoreExplanation = scoreManager.explainScore(bestSolution);
        Map<String, ConstraintMatchTotal<Score_>> constraintMatchTotals =
                scoreExplanation.getConstraintMatchTotalMap();
        assertThat(constraintMatchTotals).isNotNull();
        assertThat(constraintMatchTotals.values().stream()
                .map(ConstraintMatchTotal::getScore)
                .reduce(Score::add)
                .orElse(bestScore.zero()))
                        .isEqualTo(scoreExplanation.getScore());
        assertThat(scoreExplanation.getIndictmentMap()).isNotNull();
    }

    protected static class TestData<Score_ extends Score<Score_>> {

        public static <Score_ extends Score<Score_>> TestData<Score_> of(ConstraintStreamImplType constraintStreamImplType,
                String unsolvedDataFile, Score_ bestScoreLimitForReproducible) {
            return TestData.of(constraintStreamImplType, unsolvedDataFile, bestScoreLimitForReproducible, null);
        }

        public static <Score_ extends Score<Score_>> TestData<Score_> of(ConstraintStreamImplType constraintStreamImplType,
                String unsolvedDataFile, Score_ bestScoreLimitForReproducible, Score_ bestScoreLimitForFastAssert) {
            return TestData.of(constraintStreamImplType, unsolvedDataFile, bestScoreLimitForReproducible,
                    bestScoreLimitForFastAssert, null);
        }

        public static <Score_ extends Score<Score_>> TestData<Score_> of(ConstraintStreamImplType constraintStreamImplType,
                String unsolvedDataFile, Score_ bestScoreLimitForReproducible, Score_ bestScoreLimitForFastAssert,
                Score_ bestScoreLimitForFullAssert) {
            return new TestData<>(constraintStreamImplType, unsolvedDataFile, bestScoreLimitForReproducible,
                    bestScoreLimitForFastAssert, bestScoreLimitForFullAssert);
        }

        private final ConstraintStreamImplType constraintStreamImplType;
        private final String unsolvedDataFile;
        private final Score_ bestScoreLimitForReproducible;
        private final Score_ bestScoreLimitForFastAssert;
        private final Score_ bestScoreLimitForFullAssert;

        private TestData(ConstraintStreamImplType constraintStreamImplType, String unsolvedDataFile,
                Score_ bestScoreLimitForReproducible, Score_ bestScoreLimitForFastAssert,
                Score_ bestScoreLimitForFullAssert) {
            this.constraintStreamImplType = constraintStreamImplType;
            this.unsolvedDataFile = unsolvedDataFile;
            this.bestScoreLimitForReproducible = Objects.requireNonNull(bestScoreLimitForReproducible);
            this.bestScoreLimitForFastAssert = bestScoreLimitForFastAssert;
            this.bestScoreLimitForFullAssert = bestScoreLimitForFullAssert;
        }
    }
}
