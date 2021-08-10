/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.benchmark.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.optaplanner.benchmark.config.ProblemBenchmarksConfig;
import org.optaplanner.benchmark.config.SolverBenchmarkConfig;
import org.optaplanner.benchmark.config.statistic.ProblemStatisticType;
import org.optaplanner.benchmark.config.statistic.SingleStatisticType;
import org.optaplanner.benchmark.impl.result.PlannerBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SolverBenchmarkResult;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.config.solver.metric.MetricConfig;
import org.optaplanner.core.config.solver.metric.SolverMetric;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.solver.DefaultSolverFactory;

public class SolverBenchmarkFactory {
    private final SolverBenchmarkConfig config;

    public SolverBenchmarkFactory(SolverBenchmarkConfig config) {
        this.config = config;
    }

    public <Solution_> void buildSolverBenchmark(ClassLoader classLoader, PlannerBenchmarkResult plannerBenchmark,
            Solution_[] extraProblems) {
        validate();
        SolverBenchmarkResult solverBenchmarkResult = new SolverBenchmarkResult(plannerBenchmark);
        solverBenchmarkResult.setName(config.getName());
        solverBenchmarkResult.setSubSingleCount(ConfigUtils.inheritOverwritableProperty(config.getSubSingleCount(), 1));
        if (config.getSolverConfig().getClassLoader() == null) {
            config.getSolverConfig().setClassLoader(classLoader);
        }
        Map<String, String> additionalTagsMap = new HashMap<>();
        List<SolverMetric> solverMetricList = getSolverMetrics(config.getProblemBenchmarksConfig());
        additionalTagsMap.put("optaplanner.benchmark.name", config.getName());
        solverBenchmarkResult.setSolverConfig(config.getSolverConfig()
                .copyConfig().withMetricConfig(
                        new MetricConfig()
                                .withTagNameToValueMap(additionalTagsMap)
                                .withSolverMetricList(solverMetricList)));
        DefaultSolverFactory<Solution_> defaultSolverFactory = new DefaultSolverFactory<>(config.getSolverConfig());
        SolutionDescriptor<Solution_> solutionDescriptor =
                defaultSolverFactory.buildSolutionDescriptor(EnvironmentMode.REPRODUCIBLE);
        for (Solution_ extraProblem : extraProblems) {
            if (!solutionDescriptor.getSolutionClass().isInstance(extraProblem)) {
                throw new IllegalArgumentException("The solverBenchmark name (" + config.getName()
                        + ") for solution class (" + solutionDescriptor.getSolutionClass()
                        + ") cannot solve a problem (" + extraProblem
                        + ") of class (" + (extraProblem == null ? null : extraProblem.getClass()) + ").");
            }
        }
        solverBenchmarkResult.setScoreDefinition(solutionDescriptor.getScoreDefinition());
        solverBenchmarkResult.setSingleBenchmarkResultList(new ArrayList<>());
        ProblemBenchmarksConfig problemBenchmarksConfig_ =
                config.getProblemBenchmarksConfig() == null ? new ProblemBenchmarksConfig()
                        : config.getProblemBenchmarksConfig();
        plannerBenchmark.getSolverBenchmarkResultList().add(solverBenchmarkResult);
        ProblemBenchmarksFactory problemBenchmarksFactory = new ProblemBenchmarksFactory(problemBenchmarksConfig_);
        problemBenchmarksFactory.buildProblemBenchmarkList(solverBenchmarkResult, extraProblems);
    }

    protected void validate() {
        if (!DefaultPlannerBenchmarkFactory.VALID_NAME_PATTERN.matcher(config.getName()).matches()) {
            throw new IllegalStateException("The solverBenchmark name (" + config.getName()
                    + ") is invalid because it does not follow the nameRegex ("
                    + DefaultPlannerBenchmarkFactory.VALID_NAME_PATTERN.pattern() + ")" +
                    " which might cause an illegal filename.");
        }
        if (!config.getName().trim().equals(config.getName())) {
            throw new IllegalStateException("The solverBenchmark name (" + config.getName()
                    + ") is invalid because it starts or ends with whitespace.");
        }
        if (config.getSubSingleCount() != null && config.getSubSingleCount() < 1) {
            throw new IllegalStateException("The solverBenchmark name (" + config.getName()
                    + ") is invalid because the subSingleCount (" + config.getSubSingleCount() + ") must be greater than 1.");
        }
    }

    protected List<SolverMetric> getSolverMetrics(ProblemBenchmarksConfig config) {
        List<SolverMetric> out = new ArrayList<>();
        if (config == null) {
            return out;
        }
        for (ProblemStatisticType problemStatisticType : ObjectUtils.defaultIfNull(config.getProblemStatisticTypeList(),
                Collections.<ProblemStatisticType> emptyList())) {
            if (problemStatisticType == ProblemStatisticType.SCORE_CALCULATION_SPEED) {
                out.add(SolverMetric.SCORE_CALCULATION_COUNT);
            } else {
                out.add(SolverMetric.valueOf(problemStatisticType.name()));
            }
        }
        for (SingleStatisticType singleStatisticType : ObjectUtils.defaultIfNull(config.getSingleStatisticTypeList(),
                Collections.<SingleStatisticType> emptyList())) {
            out.add(SolverMetric.valueOf(singleStatisticType.name()));
        }
        return out;
    }
}
