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

package org.optaplanner.benchmark.impl.statistic.memoryuse;

import java.util.List;
import java.util.function.Consumer;

import org.optaplanner.benchmark.config.statistic.ProblemStatisticType;
import org.optaplanner.benchmark.impl.result.SubSingleBenchmarkResult;
import org.optaplanner.benchmark.impl.statistic.ProblemBasedSubSingleStatistic;
import org.optaplanner.benchmark.impl.statistic.StatisticRegistry;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.solver.metric.SolverMetric;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;

import io.micrometer.core.instrument.Tags;

public class MemoryUseSubSingleStatistic<Solution_>
        extends ProblemBasedSubSingleStatistic<Solution_, MemoryUseStatisticPoint> {

    private long timeMillisThresholdInterval;

    public MemoryUseSubSingleStatistic(SubSingleBenchmarkResult subSingleBenchmarkResult) {
        this(subSingleBenchmarkResult, 1000L);
    }

    public MemoryUseSubSingleStatistic(SubSingleBenchmarkResult subSingleBenchmarkResult, long timeMillisThresholdInterval) {
        super(subSingleBenchmarkResult, ProblemStatisticType.MEMORY_USE);
        if (timeMillisThresholdInterval <= 0L) {
            throw new IllegalArgumentException("The timeMillisThresholdInterval (" + timeMillisThresholdInterval
                    + ") must be bigger than 0.");
        }
        this.timeMillisThresholdInterval = timeMillisThresholdInterval;
    }

    // ************************************************************************
    // Lifecycle methods
    // ************************************************************************

    @Override
    public void open(StatisticRegistry registry, Tags runTag, Solver<Solution_> solver) {
        registry.addListener(SolverMetric.MEMORY_USE, new MemoryUseSubSingleStatisticListener(registry, runTag));
    }

    @Override
    public void close(StatisticRegistry registry, Tags runTag, Solver<Solution_> solver) {
    }

    private class MemoryUseSubSingleStatisticListener implements Consumer<Long> {

        private long nextTimeMillisThreshold = timeMillisThresholdInterval;
        private StatisticRegistry registry;
        private Tags tags;

        public MemoryUseSubSingleStatisticListener(StatisticRegistry registry, Tags tags) {
            this.registry = registry;
            this.tags = tags;
        }

        @Override
        public void accept(Long timeMillisSpent) {
            if (timeMillisSpent >= nextTimeMillisThreshold) {
                registry.getGaugeValue(SolverMetric.MEMORY_USE, tags, memoryUse -> {
                    pointList.add(new MemoryUseStatisticPoint(timeMillisSpent, new MemoryUseMeasurement(memoryUse.longValue(),
                            (long) registry.find("jvm.memory.max").tags(tags).gauge().value())));
                });

                nextTimeMillisThreshold += timeMillisThresholdInterval;
                if (nextTimeMillisThreshold < timeMillisSpent) {
                    nextTimeMillisThreshold = timeMillisSpent;
                }
            }
        }

    }

    // ************************************************************************
    // CSV methods
    // ************************************************************************

    @Override
    protected String getCsvHeader() {
        return MemoryUseStatisticPoint.buildCsvLine("timeMillisSpent", "usedMemory", "maxMemory");
    }

    @Override
    protected MemoryUseStatisticPoint createPointFromCsvLine(ScoreDefinition scoreDefinition,
            List<String> csvLine) {
        return new MemoryUseStatisticPoint(Long.parseLong(csvLine.get(0)),
                new MemoryUseMeasurement(Long.parseLong(csvLine.get(1)), Long.parseLong(csvLine.get(2))));
    }

}
