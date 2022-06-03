/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.constraint.drl;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.EnvironmentMode;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.director.AbstractScoreDirectorSemanticsTest;
import org.optaplanner.core.impl.score.director.InnerScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactoryFactory;

final class DrlScoreDirectorSemanticsTest extends AbstractScoreDirectorSemanticsTest {

    @Override
    protected <Solution_> InnerScoreDirectorFactory<Solution_, SimpleScore>
            buildInnerScoreDirectorFactory(SolutionDescriptor<Solution_> solutionDescriptor) {
        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = new ScoreDirectorFactoryConfig()
                .withScoreDrls("org/optaplanner/constraint/drl/scoreDirectorSemanticsDroolsConstraints.drl",
                        "org/optaplanner/constraint/drl/scoreDirectorSemanticsDroolsConstraints2.drl");
        ScoreDirectorFactoryFactory<Solution_, SimpleScore> scoreDirectorFactoryFactory =
                new ScoreDirectorFactoryFactory<>(scoreDirectorFactoryConfig);
        return scoreDirectorFactoryFactory.buildScoreDirectorFactory(getClass().getClassLoader(),
                EnvironmentMode.REPRODUCIBLE, solutionDescriptor);
    }
}
