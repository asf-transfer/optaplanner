package org.optaplanner.core.impl.phase;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.exhaustivesearch.ExhaustiveSearchPhaseConfig;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.partitionedsearch.PartitionedSearchPhaseConfig;
import org.optaplanner.core.config.phase.NoChangePhaseConfig;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.phase.custom.CustomPhaseConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.constructionheuristic.DefaultConstructionHeuristicPhaseFactory;
import org.optaplanner.core.impl.exhaustivesearch.DefaultExhaustiveSearchPhaseFactory;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.localsearch.DefaultLocalSearchPhaseFactory;
import org.optaplanner.core.impl.partitionedsearch.DefaultPartitionedSearchPhaseFactory;
import org.optaplanner.core.impl.phase.custom.DefaultCustomPhaseFactory;
import org.optaplanner.core.impl.solver.ClassInstanceCache;
import org.optaplanner.core.impl.solver.recaller.BestSolutionRecaller;
import org.optaplanner.core.impl.solver.termination.Termination;

public interface PhaseFactory<Solution_> {

    static <Solution_> PhaseFactory<Solution_> create(PhaseConfig<?> phaseConfig, ClassInstanceCache instanceCache) {
        if (LocalSearchPhaseConfig.class.isAssignableFrom(phaseConfig.getClass())) {
            return new DefaultLocalSearchPhaseFactory<>((LocalSearchPhaseConfig) phaseConfig, instanceCache);
        } else if (ConstructionHeuristicPhaseConfig.class.isAssignableFrom(phaseConfig.getClass())) {
            return new DefaultConstructionHeuristicPhaseFactory<>((ConstructionHeuristicPhaseConfig) phaseConfig,
                    instanceCache);
        } else if (PartitionedSearchPhaseConfig.class.isAssignableFrom(phaseConfig.getClass())) {
            return new DefaultPartitionedSearchPhaseFactory<>((PartitionedSearchPhaseConfig) phaseConfig, instanceCache);
        } else if (CustomPhaseConfig.class.isAssignableFrom(phaseConfig.getClass())) {
            return new DefaultCustomPhaseFactory<>((CustomPhaseConfig) phaseConfig, instanceCache);
        } else if (ExhaustiveSearchPhaseConfig.class.isAssignableFrom(phaseConfig.getClass())) {
            return new DefaultExhaustiveSearchPhaseFactory<>((ExhaustiveSearchPhaseConfig) phaseConfig, instanceCache);
        } else if (NoChangePhaseConfig.class.isAssignableFrom(phaseConfig.getClass())) {
            return new NoChangePhaseFactory<>((NoChangePhaseConfig) phaseConfig, instanceCache);
        } else {
            throw new IllegalArgumentException(String.format("Unknown %s type: (%s).",
                    PhaseConfig.class.getSimpleName(), phaseConfig.getClass().getName()));
        }
    }

    static <Solution_> List<Phase<Solution_>> buildPhases(List<PhaseConfig> phaseConfigList,
            HeuristicConfigPolicy<Solution_> configPolicy, BestSolutionRecaller<Solution_> bestSolutionRecaller,
            Termination<Solution_> termination, ClassInstanceCache instanceCache) {
        List<Phase<Solution_>> phaseList = new ArrayList<>(phaseConfigList.size());
        for (int phaseIndex = 0; phaseIndex < phaseConfigList.size(); phaseIndex++) {
            PhaseConfig phaseConfig = phaseConfigList.get(phaseIndex);
            if (phaseIndex > 0) {
                PhaseConfig previousPhaseConfig = phaseConfigList.get(phaseIndex - 1);
                if (!willTerminate(previousPhaseConfig)) {
                    throw new IllegalStateException("Solver configuration contains an unreachable phase. "
                            + "Phase #" + phaseIndex + " (" + phaseConfig + ") follows a phase "
                            + "without a configured termination (" + previousPhaseConfig + ").");
                }
            }
            PhaseFactory<Solution_> phaseFactory = PhaseFactory.create(phaseConfig, instanceCache);
            Phase<Solution_> phase =
                    phaseFactory.buildPhase(phaseIndex, configPolicy, bestSolutionRecaller, termination);
            phaseList.add(phase);
        }
        return phaseList;
    }

    private static boolean willTerminate(PhaseConfig phaseConfig) {
        if (phaseConfig instanceof ConstructionHeuristicPhaseConfig
                || phaseConfig instanceof ExhaustiveSearchPhaseConfig
                || phaseConfig instanceof CustomPhaseConfig) { // Termination guaranteed.
            return true;
        }
        TerminationConfig terminationConfig = phaseConfig.getTerminationConfig();
        return (terminationConfig != null && terminationConfig.isConfigured());
    }

    Phase<Solution_> buildPhase(int phaseIndex, HeuristicConfigPolicy<Solution_> solverConfigPolicy,
            BestSolutionRecaller<Solution_> bestSolutionRecaller, Termination<Solution_> solverTermination);
}
