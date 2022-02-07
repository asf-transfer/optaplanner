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

package org.optaplanner.constraint.drl.testgen;

import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaplanner.constraint.drl.AbstractDrlScoreDirectorFactoryService;
import org.optaplanner.constraint.drl.AbstractDrlScoreDirectorFactoryServiceTest;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.impl.score.director.AbstractScoreDirectorFactory;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;

class TestGenDrlScoreDirectorFactoryServiceTest extends AbstractDrlScoreDirectorFactoryServiceTest {

    @BeforeEach
    void enableTestGen() {
        System.setProperty(AbstractDrlScoreDirectorFactoryService.GENERATE_DROOLS_TEST_ON_ERROR_PROPERTY_NAME, "true");
    }

    @AfterEach
    void disableTestGen() {
        System.clearProperty(AbstractDrlScoreDirectorFactoryService.GENERATE_DROOLS_TEST_ON_ERROR_PROPERTY_NAME);
    }

    @Override
    @Test
    protected void testGenSwitched() {
        disableTestGen();
        ScoreDirectorFactoryConfig config = new ScoreDirectorFactoryConfig()
                .withScoreDrls("org/optaplanner/constraint/drl/invalidDroolsConstraints.drl");
        Assertions.assertThat(buildScoreDirectoryFactory(config)).isNull();
    }

    @Override
    protected ScoreDirectorFactory<TestdataSolution> buildScoreDirectoryFactory(ScoreDirectorFactoryConfig config) {
        Supplier<AbstractScoreDirectorFactory<TestdataSolution, SimpleScore>> supplier =
                new TestGenDrlScoreDirectorFactoryService<TestdataSolution, SimpleScore>()
                        .buildScoreDirectorFactory(getClass().getClassLoader(),
                                TestdataSolution.buildSolutionDescriptor(), config);
        if (supplier == null) {
            return null;
        }
        return supplier.get();
    }
}
