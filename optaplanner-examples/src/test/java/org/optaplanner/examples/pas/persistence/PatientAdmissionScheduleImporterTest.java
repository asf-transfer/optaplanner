/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.examples.pas.persistence;

import org.optaplanner.examples.common.persistence.AbstractSolutionImporter;
import org.optaplanner.examples.common.persistence.ImportDataFilesTest;
import org.optaplanner.examples.pas.app.PatientAdmissionScheduleApp;
import org.optaplanner.examples.pas.domain.PatientAdmissionSchedule;

class PatientAdmissionScheduleImporterTest extends ImportDataFilesTest<PatientAdmissionSchedule> {

    @Override
    protected AbstractSolutionImporter<PatientAdmissionSchedule> createSolutionImporter() {
        return new PatientAdmissionScheduleImporter();
    }

    @Override
    protected String getDataDirName() {
        return PatientAdmissionScheduleApp.DATA_DIR_NAME;
    }
}
