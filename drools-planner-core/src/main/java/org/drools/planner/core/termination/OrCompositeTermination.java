/*
 * Copyright 2010 JBoss Inc
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

package org.drools.planner.core.termination;

import org.drools.planner.core.phase.AbstractSolverPhaseScope;
import org.drools.planner.core.phase.step.AbstractStepScope;
import org.drools.planner.core.solver.DefaultSolverScope;

public class OrCompositeTermination extends AbstractCompositeTermination {

    public OrCompositeTermination() {
    }

    public OrCompositeTermination(Termination... terminations) {
        super(terminations);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    /**
     * @param solverScope never null
     * @return true if any of the Termination is terminated.
     */
    public boolean isSolverTerminated(DefaultSolverScope solverScope) {
        for (Termination termination : terminationList) {
            if (termination.isSolverTerminated(solverScope)) {
                return true;
            }
        }
        return false;
    }
    /**
     * @param solverPhaseScope never null
     * @return true if any of the Termination is terminated.
     */
    public boolean isPhaseTerminated(AbstractSolverPhaseScope solverPhaseScope) {
        for (Termination termination : terminationList) {
            if (termination.isPhaseTerminated(solverPhaseScope)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the minimum timeGradient of all Terminations.
     * Not supported timeGradients (-1.0) are ignored.
     * @param solverScope never null
     * @return the maximum timeGradient of the Terminations.
     */
    public double calculateSolverTimeGradient(DefaultSolverScope solverScope) {
        double timeGradient = 0.0;
        for (Termination termination : terminationList) {
            double nextTimeGradient = termination.calculateSolverTimeGradient(solverScope);
            if (nextTimeGradient >= 0.0) {
                timeGradient = Math.max(timeGradient, nextTimeGradient);
            }
        }
        return timeGradient;
    }

    /**
     * Calculates the minimum timeGradient of all Terminations.
     * Not supported timeGradients (-1.0) are ignored.
     * @param solverPhaseScope never null
     * @return the maximum timeGradient of the Terminations.
     */
    public double calculatePhaseTimeGradient(AbstractSolverPhaseScope solverPhaseScope) {
        double timeGradient = 0.0;
        for (Termination termination : terminationList) {
            double nextTimeGradient = termination.calculatePhaseTimeGradient(solverPhaseScope);
            if (nextTimeGradient >= 0.0) {
                timeGradient = Math.max(timeGradient, nextTimeGradient);
            }
        }
        return timeGradient;
    }

}
