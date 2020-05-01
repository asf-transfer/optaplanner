package org.optaplanner.core.impl.solver.termination;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;

public class OrCompositeTerminationTest {

    @Test
    public void solveTermination() {
        Termination termination1 = mock(Termination.class);
        Termination termination2 = mock(Termination.class);

        Termination compositeTermination = new OrCompositeTermination(termination1, termination2);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);

        when(termination1.isSolverTerminated(solverScope)).thenReturn(false);
        when(termination2.isSolverTerminated(solverScope)).thenReturn(false);
        assertFalse(compositeTermination.isSolverTerminated(solverScope));

        when(termination1.isSolverTerminated(solverScope)).thenReturn(true);
        when(termination2.isSolverTerminated(solverScope)).thenReturn(false);
        assertTrue(compositeTermination.isSolverTerminated(solverScope));

        when(termination1.isSolverTerminated(solverScope)).thenReturn(false);
        when(termination2.isSolverTerminated(solverScope)).thenReturn(true);
        assertTrue(compositeTermination.isSolverTerminated(solverScope));

        when(termination1.isSolverTerminated(solverScope)).thenReturn(true);
        when(termination2.isSolverTerminated(solverScope)).thenReturn(true);
        assertTrue(compositeTermination.isSolverTerminated(solverScope));
    }

    @Test
    public void phaseTermination() {
        Termination termination1 = mock(Termination.class);
        Termination termination2 = mock(Termination.class);

        Termination compositeTermination = new OrCompositeTermination(Arrays.asList(termination1, termination2));

        AbstractPhaseScope phaseScope = mock(AbstractPhaseScope.class);

        when(termination1.isPhaseTerminated(phaseScope)).thenReturn(false);
        when(termination2.isPhaseTerminated(phaseScope)).thenReturn(false);
        assertFalse(compositeTermination.isPhaseTerminated(phaseScope));

        when(termination1.isPhaseTerminated(phaseScope)).thenReturn(true);
        when(termination2.isPhaseTerminated(phaseScope)).thenReturn(false);
        assertTrue(compositeTermination.isPhaseTerminated(phaseScope));

        when(termination1.isPhaseTerminated(phaseScope)).thenReturn(false);
        when(termination2.isPhaseTerminated(phaseScope)).thenReturn(true);
        assertTrue(compositeTermination.isPhaseTerminated(phaseScope));

        when(termination1.isPhaseTerminated(phaseScope)).thenReturn(true);
        when(termination2.isPhaseTerminated(phaseScope)).thenReturn(true);
        assertTrue(compositeTermination.isPhaseTerminated(phaseScope));
    }

    @Test
    public void calculateSolverTimeGradientTest() {
        Termination termination1 = mock(Termination.class);
        Termination termination2 = mock(Termination.class);

        Termination compositeTermination = new OrCompositeTermination(Arrays.asList(termination1, termination2));

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);

        when(termination1.calculateSolverTimeGradient(solverScope)).thenReturn(0.0);
        when(termination2.calculateSolverTimeGradient(solverScope)).thenReturn(0.0);
        // max(0.0,0.0) = 0.0
        assertEquals(0.0, compositeTermination.calculateSolverTimeGradient(solverScope), 0.0);

        when(termination1.calculateSolverTimeGradient(solverScope)).thenReturn(0.5);
        when(termination2.calculateSolverTimeGradient(solverScope)).thenReturn(0.0);
        // max(0.5,0.0) = 0.5
        assertEquals(0.5, compositeTermination.calculateSolverTimeGradient(solverScope), 0.0);

        when(termination1.calculateSolverTimeGradient(solverScope)).thenReturn(0.0);
        when(termination2.calculateSolverTimeGradient(solverScope)).thenReturn(0.5);
        // max(0.0,0.5) = 0.5
        assertEquals(0.5, compositeTermination.calculateSolverTimeGradient(solverScope), 0.0);

        when(termination1.calculateSolverTimeGradient(solverScope)).thenReturn(-1.0);
        when(termination2.calculateSolverTimeGradient(solverScope)).thenReturn(-1.0);
        // Negative time gradient values are unsupported and ignored, max(unsupported,unsupported) = 0.0 (default)
        assertEquals(0.0, compositeTermination.calculateSolverTimeGradient(solverScope), 0.0);

        when(termination1.calculateSolverTimeGradient(solverScope)).thenReturn(0.5);
        when(termination2.calculateSolverTimeGradient(solverScope)).thenReturn(-1.0);
        // Negative time gradient values are unsupported and ignored, max(0.5,unsupported) = 0.5
        assertEquals(0.5, compositeTermination.calculateSolverTimeGradient(solverScope), 0.0);

        when(termination1.calculateSolverTimeGradient(solverScope)).thenReturn(-1.0);
        when(termination2.calculateSolverTimeGradient(solverScope)).thenReturn(0.5);
        // Negative time gradient values are unsupported and ignored, max(unsupported,0.5) = 0.5
        assertEquals(0.5, compositeTermination.calculateSolverTimeGradient(solverScope), 0.0);
    }

    @Test
    public void calculatePhaseTimeGradientTest() {
        Termination termination1 = mock(Termination.class);
        Termination termination2 = mock(Termination.class);

        Termination compositeTermination = new OrCompositeTermination(Arrays.asList(termination1, termination2));

        AbstractPhaseScope phaseScope = mock(AbstractPhaseScope.class);

        when(termination1.calculatePhaseTimeGradient(phaseScope)).thenReturn(0.0);
        when(termination2.calculatePhaseTimeGradient(phaseScope)).thenReturn(0.0);
        // max(0.0,0.0) = 0.0
        assertEquals(0.0, compositeTermination.calculatePhaseTimeGradient(phaseScope), 0.0);

        when(termination1.calculatePhaseTimeGradient(phaseScope)).thenReturn(0.5);
        when(termination2.calculatePhaseTimeGradient(phaseScope)).thenReturn(0.0);
        // max(0.5,0.0) = 0.5
        assertEquals(0.5, compositeTermination.calculatePhaseTimeGradient(phaseScope), 0.0);

        when(termination1.calculatePhaseTimeGradient(phaseScope)).thenReturn(0.0);
        when(termination2.calculatePhaseTimeGradient(phaseScope)).thenReturn(0.5);
        // max(0.0,0.5) = 0.5
        assertEquals(0.5, compositeTermination.calculatePhaseTimeGradient(phaseScope), 0.0);

        when(termination1.calculatePhaseTimeGradient(phaseScope)).thenReturn(-1.0);
        when(termination2.calculatePhaseTimeGradient(phaseScope)).thenReturn(-1.0);
        // Negative time gradient values are unsupported and ignored, max(unsupported,unsupported) = 0.0 (default)
        assertEquals(0.0, compositeTermination.calculatePhaseTimeGradient(phaseScope), 0.0);

        when(termination1.calculatePhaseTimeGradient(phaseScope)).thenReturn(0.5);
        when(termination2.calculatePhaseTimeGradient(phaseScope)).thenReturn(-1.0);
        // Negative time gradient values are unsupported and ignored, max(0.5,unsupported) = 0.5
        assertEquals(0.5, compositeTermination.calculatePhaseTimeGradient(phaseScope), 0.0);

        when(termination1.calculatePhaseTimeGradient(phaseScope)).thenReturn(-1.0);
        when(termination2.calculatePhaseTimeGradient(phaseScope)).thenReturn(0.5);
        // Negative time gradient values are unsupported and ignored, max(unsupported,0.5) = 0.5
        assertEquals(0.5, compositeTermination.calculatePhaseTimeGradient(phaseScope), 0.0);
    }
}
