/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.benchmark.impl.statistic.subsingle.constraintmatchtotalstepscore;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.optaplanner.benchmark.config.statistic.SingleStatisticType;
import org.optaplanner.benchmark.impl.report.BenchmarkReport;
import org.optaplanner.benchmark.impl.result.SubSingleBenchmarkResult;
import org.optaplanner.benchmark.impl.statistic.PureSubSingleStatistic;
import org.optaplanner.benchmark.impl.statistic.StatisticRegistry;
import org.optaplanner.benchmark.impl.statistic.common.MillisecondsSpentNumberFormat;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.solver.metric.SolverMetric;
import org.optaplanner.core.impl.score.ScoreUtils;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.solver.DefaultSolver;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;

public class ConstraintMatchTotalStepScoreSubSingleStatistic<Solution_>
        extends PureSubSingleStatistic<Solution_, ConstraintMatchTotalStepScoreStatisticPoint> {

    @XmlTransient
    protected List<File> graphFileList = null;

    public ConstraintMatchTotalStepScoreSubSingleStatistic(SubSingleBenchmarkResult subSingleBenchmarkResult) {
        super(subSingleBenchmarkResult, SingleStatisticType.CONSTRAINT_MATCH_TOTAL_STEP_SCORE);
    }

    /**
     * @return never null
     */
    @Override
    public List<File> getGraphFileList() {
        return graphFileList;
    }

    // ************************************************************************
    // Lifecycle methods
    // ************************************************************************

    @Override
    public void open(StatisticRegistry registry, Tags runTag, Solver<Solution_> solver) {
        DefaultSolver<Solution_> defaultSolver = (DefaultSolver<Solution_>) solver;
        defaultSolver.getSolverScope().getScoreDirector().overwriteConstraintMatchEnabledPreference(true);
        registry.addListener(SolverMetric.CONSTRAINT_MATCH_TOTAL_STEP_SCORE, timeMillisSpent -> {
            Set<Meter.Id> meterIds = registry.getMeterIds(SolverMetric.CONSTRAINT_MATCH_TOTAL_STEP_SCORE, runTag);
            Set<ImmutablePair<String, String>> constraintPackageNamePairs = new HashSet<>();
            meterIds.forEach(meterId -> {
                constraintPackageNamePairs
                        .add(ImmutablePair.of(meterId.getTag("constraint.package"), meterId.getTag("constraint.name")));
            });
            constraintPackageNamePairs.forEach(constraintPackageNamePair -> {
                String constraintPackage = constraintPackageNamePair.left;
                String constraintName = constraintPackageNamePair.right;
                registry.extractScoreFromMeters(SolverMetric.CONSTRAINT_MATCH_TOTAL_STEP_SCORE, runTag
                        .and("constraint.package", constraintPackageNamePair.left)
                        .and("constraint.name", constraintPackageNamePair.right), score -> {
                            registry.getGaugeValue(SolverMetric.CONSTRAINT_MATCH_TOTAL_STEP_SCORE + ".count",
                                    runTag.and("constraint.package", constraintPackageNamePair.left)
                                            .and("constraint.name", constraintPackageNamePair.right),
                                    count -> {
                                        pointList.add(new ConstraintMatchTotalStepScoreStatisticPoint(
                                                timeMillisSpent,
                                                constraintPackage,
                                                constraintName,
                                                count.intValue(),
                                                score));
                                    });
                        });
            });
        });
    }

    @Override
    public void close(StatisticRegistry registry, Tags runTag, Solver<Solution_> solver) {
    }

    // ************************************************************************
    // CSV methods
    // ************************************************************************

    @Override
    protected String getCsvHeader() {
        return ConstraintMatchTotalStepScoreStatisticPoint.buildCsvLine(
                "timeMillisSpent", "constraintPackage", "constraintName",
                "constraintMatchCount", "scoreTotal");
    }

    @Override
    protected ConstraintMatchTotalStepScoreStatisticPoint createPointFromCsvLine(ScoreDefinition scoreDefinition,
            List<String> csvLine) {
        return new ConstraintMatchTotalStepScoreStatisticPoint(Long.parseLong(csvLine.get(0)),
                csvLine.get(1), csvLine.get(2),
                Integer.parseInt(csvLine.get(3)), scoreDefinition.parseScore(csvLine.get(4)));
    }

    // ************************************************************************
    // Write methods
    // ************************************************************************

    @Override
    public void writeGraphFiles(BenchmarkReport benchmarkReport) {
        List<Map<String, XYSeries>> constraintIdToWeightSeriesMapList = new ArrayList<>(
                BenchmarkReport.CHARTED_SCORE_LEVEL_SIZE);
        for (ConstraintMatchTotalStepScoreStatisticPoint point : getPointList()) {
            long timeMillisSpent = point.getTimeMillisSpent();
            double[] levelValues = ScoreUtils.extractLevelDoubles(point.getScoreTotal());
            for (int i = 0; i < levelValues.length && i < BenchmarkReport.CHARTED_SCORE_LEVEL_SIZE; i++) {
                if (i >= constraintIdToWeightSeriesMapList.size()) {
                    constraintIdToWeightSeriesMapList.add(new LinkedHashMap<>());
                }
                Map<String, XYSeries> constraintIdToWeightSeriesMap = constraintIdToWeightSeriesMapList.get(i);
                XYSeries weightSeries = constraintIdToWeightSeriesMap.computeIfAbsent(point.getConstraintId(),
                        k -> new XYSeries(point.getConstraintName() + " weight"));
                // Only add changes
                if (levelValues[i] != ((weightSeries.getItemCount() == 0) ? 0.0
                        : weightSeries.getY(weightSeries.getItemCount() - 1).doubleValue())) {
                    weightSeries.add(timeMillisSpent, levelValues[i]);
                }
            }
        }
        long timeMillisSpent = subSingleBenchmarkResult.getTimeMillisSpent();
        for (Map<String, XYSeries> constraintIdToWeightSeriesMap : constraintIdToWeightSeriesMapList) {
            for (Iterator<Map.Entry<String, XYSeries>> it = constraintIdToWeightSeriesMap.entrySet().iterator(); it
                    .hasNext();) {
                XYSeries weightSeries = it.next().getValue();
                if (weightSeries.getItemCount() == 0) {
                    // Only show the constraint type on the score levels that it affects
                    it.remove();
                } else {
                    // Draw a horizontal line from the last new best step to how long the solver actually ran
                    weightSeries.add(timeMillisSpent, weightSeries.getY(weightSeries.getItemCount() - 1).doubleValue());
                }
            }
        }
        graphFileList = new ArrayList<>(constraintIdToWeightSeriesMapList.size());
        for (int scoreLevelIndex = 0; scoreLevelIndex < constraintIdToWeightSeriesMapList.size(); scoreLevelIndex++) {
            XYPlot plot = createPlot(benchmarkReport, scoreLevelIndex);
            // No direct ascending lines between 2 points, but a stepping line instead
            XYItemRenderer renderer = new XYStepRenderer();
            plot.setRenderer(renderer);
            XYSeriesCollection seriesCollection = new XYSeriesCollection();
            for (XYSeries series : constraintIdToWeightSeriesMapList.get(scoreLevelIndex).values()) {
                seriesCollection.addSeries(series);
            }
            plot.setDataset(seriesCollection);
            String scoreLevelLabel = subSingleBenchmarkResult.getSingleBenchmarkResult().getProblemBenchmarkResult()
                    .findScoreLevelLabel(scoreLevelIndex);
            JFreeChart chart = new JFreeChart(subSingleBenchmarkResult.getName()
                    + " constraint match total step " + scoreLevelLabel + " diff statistic",
                    JFreeChart.DEFAULT_TITLE_FONT, plot, true);
            graphFileList.add(writeChartToImageFile(chart,
                    "ConstraintMatchTotalStepScoreStatisticLevel" + scoreLevelIndex));
        }
    }

    private XYPlot createPlot(BenchmarkReport benchmarkReport, int scoreLevelIndex) {
        Locale locale = benchmarkReport.getLocale();
        NumberAxis xAxis = new NumberAxis("Time spent");
        xAxis.setNumberFormatOverride(new MillisecondsSpentNumberFormat(locale));
        String scoreLevelLabel = subSingleBenchmarkResult.getSingleBenchmarkResult().getProblemBenchmarkResult()
                .findScoreLevelLabel(scoreLevelIndex);
        NumberAxis yAxis = new NumberAxis("Constraint match total " + scoreLevelLabel);
        yAxis.setNumberFormatOverride(NumberFormat.getInstance(locale));
        yAxis.setAutoRangeIncludesZero(false);
        XYPlot plot = new XYPlot(null, xAxis, yAxis, null);
        plot.setOrientation(PlotOrientation.VERTICAL);
        return plot;
    }

}
