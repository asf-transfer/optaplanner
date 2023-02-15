package org.optaplanner.core.impl.heuristic.selector.move.generic.list.kopt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;

/**
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <Node_> The value type
 */
public class KOptListMove<Solution_, Node_> extends AbstractMove<Solution_> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final Object entity;
    private final KOptDescriptor<Solution_, Node_> descriptor;
    private final List<FlipSublistAction<Solution_>> equivalent2Opts;
    private final KOptAffectedElementsInfo affectedElementsInfo;
    private final int postShiftAmount;

    public KOptListMove(ListVariableDescriptor<Solution_> listVariableDescriptor,
            Object entity,
            KOptDescriptor<Solution_, Node_> descriptor,
            List<FlipSublistAction<Solution_>> equivalent2Opts,
            int postShiftAmount) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.entity = entity;
        this.descriptor = descriptor;
        this.equivalent2Opts = equivalent2Opts;
        this.postShiftAmount = postShiftAmount;
        if (equivalent2Opts.isEmpty()) {
            affectedElementsInfo = KOptAffectedElementsInfo.forMiddleRange(0, 0);
        } else if (postShiftAmount != 0) {
            affectedElementsInfo = KOptAffectedElementsInfo.forMiddleRange(0, listVariableDescriptor.getListSize(entity));
        } else {
            KOptAffectedElementsInfo currentAffectedElements = equivalent2Opts.get(0).getAffectedElements();
            for (int i = 1; i < equivalent2Opts.size(); i++) {
                currentAffectedElements = currentAffectedElements.merge(equivalent2Opts.get(i).getAffectedElements());
            }
            affectedElementsInfo = currentAffectedElements;
        }
    }

    @Override
    protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
        if (equivalent2Opts.isEmpty()) {
            return this;
        } else {
            List<FlipSublistAction<Solution_>> inverse2Opts = new ArrayList<>(equivalent2Opts.size());
            for (int i = equivalent2Opts.size() - 1; i >= 0; i--) {
                inverse2Opts.add(equivalent2Opts.get(i).createUndoMove());
            }
            return new UndoKOptListMove<>(listVariableDescriptor, entity, descriptor, inverse2Opts, -postShiftAmount,
                    affectedElementsInfo);
        }
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
        InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
        if (affectedElementsInfo.getWrappedStartIndex() != -1) {
            innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, entity,
                    affectedElementsInfo.getWrappedStartIndex(),
                    listVariableDescriptor.getListSize(entity));
            innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, entity, 0,
                    affectedElementsInfo.getWrappedEndIndex());
        }
        for (IntIntImmutablePair affectedInterval : affectedElementsInfo.getAffectedMiddleRangeList()) {
            innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, entity, affectedInterval.leftInt(),
                    affectedInterval.rightInt());
        }

        for (FlipSublistAction<Solution_> move : equivalent2Opts) {
            move.doMoveOnGenuineVariables();
        }
        rotateToOriginalPositions(listVariableDescriptor, entity, postShiftAmount);

        if (affectedElementsInfo.getWrappedStartIndex() != -1) {
            innerScoreDirector.afterListVariableChanged(listVariableDescriptor, entity,
                    affectedElementsInfo.getWrappedStartIndex(),
                    listVariableDescriptor.getListSize(entity));
            innerScoreDirector.afterListVariableChanged(listVariableDescriptor, entity, 0,
                    affectedElementsInfo.getWrappedEndIndex());
        }
        for (IntIntImmutablePair affectedInterval : affectedElementsInfo.getAffectedMiddleRangeList()) {
            innerScoreDirector.afterListVariableChanged(listVariableDescriptor, entity, affectedInterval.leftInt(),
                    affectedInterval.rightInt());
        }
    }

    private static <Solution_> void rotateToOriginalPositions(ListVariableDescriptor<Solution_> listVariableDescriptor,
            Object entity, int shiftAmount) {
        List<Object> listVariable = listVariableDescriptor.getListVariable(entity);
        Collections.rotate(listVariable, shiftAmount);
    }

    @Override
    public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
        return !equivalent2Opts.isEmpty();
    }

    @Override
    public Move<Solution_> rebase(ScoreDirector<Solution_> destinationScoreDirector) {
        List<FlipSublistAction<Solution_>> rebasedEquivalent2Opts = new ArrayList<>(equivalent2Opts.size());
        for (FlipSublistAction<Solution_> twoOpt : equivalent2Opts) {
            rebasedEquivalent2Opts.add(twoOpt.rebase(destinationScoreDirector));
        }
        return new KOptListMove<>(listVariableDescriptor, destinationScoreDirector.lookUpWorkingObject(entity),
                descriptor, rebasedEquivalent2Opts, postShiftAmount);
    }

    @Override
    public String getSimpleMoveTypeDescription() {
        return descriptor.getK() + "-opt(" + listVariableDescriptor.getSimpleEntityAndVariableName() + ")";
    }

    @Override
    public Collection<?> getPlanningEntities() {
        return List.of(entity);
    }

    @Override
    public Collection<?> getPlanningValues() {
        List<Object> out = new ArrayList<>();
        List<Object> listVariable = listVariableDescriptor.getListVariable(entity);

        if (affectedElementsInfo.getWrappedStartIndex() != -1) {
            out.addAll(listVariable.subList(affectedElementsInfo.getWrappedStartIndex(), listVariable.size()));
            out.addAll(listVariable.subList(0, affectedElementsInfo.getWrappedEndIndex()));
        }
        for (IntIntImmutablePair affectedInterval : affectedElementsInfo.getAffectedMiddleRangeList()) {
            out.addAll(listVariable.subList(affectedInterval.leftInt(), affectedInterval.rightInt()));
        }

        return out;
    }

    public String toString() {
        return descriptor.toString();
    }

    /**
     * A K-Opt move that does the list rotation before performing the flips instead of after, allowing
     * it to act as the undo move of a K-Opt move that does the rotation after the flips.
     *
     * @param <Solution_>
     */
    private static final class UndoKOptListMove<Solution_, Node_> extends AbstractMove<Solution_> {
        private final ListVariableDescriptor<Solution_> listVariableDescriptor;
        private final Object entity;
        private final KOptDescriptor<Solution_, Node_> descriptor;
        private final List<FlipSublistAction<Solution_>> equivalent2Opts;
        private final KOptAffectedElementsInfo affectedElementsInfo;
        private final int preShiftAmount;

        public UndoKOptListMove(ListVariableDescriptor<Solution_> listVariableDescriptor,
                Object entity,
                KOptDescriptor<Solution_, Node_> descriptor,
                List<FlipSublistAction<Solution_>> equivalent2Opts,
                int preShiftAmount,
                KOptAffectedElementsInfo affectedElementsInfo) {
            this.listVariableDescriptor = listVariableDescriptor;
            this.entity = entity;
            this.descriptor = descriptor;
            this.equivalent2Opts = equivalent2Opts;
            this.preShiftAmount = preShiftAmount;
            this.affectedElementsInfo = affectedElementsInfo;
        }

        @Override
        public boolean isMoveDoable(ScoreDirector<Solution_> scoreDirector) {
            return true;
        }

        @Override
        protected AbstractMove<Solution_> createUndoMove(ScoreDirector<Solution_> scoreDirector) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void doMoveOnGenuineVariables(ScoreDirector<Solution_> scoreDirector) {
            InnerScoreDirector<Solution_, ?> innerScoreDirector = (InnerScoreDirector<Solution_, ?>) scoreDirector;
            if (affectedElementsInfo.getWrappedStartIndex() != -1) {
                innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, entity,
                        affectedElementsInfo.getWrappedStartIndex(),
                        listVariableDescriptor.getListSize(entity));
                innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, entity, 0,
                        affectedElementsInfo.getWrappedEndIndex());
            }
            for (IntIntImmutablePair affectedInterval : affectedElementsInfo.getAffectedMiddleRangeList()) {
                innerScoreDirector.beforeListVariableChanged(listVariableDescriptor, entity, affectedInterval.leftInt(),
                        affectedInterval.rightInt());
            }

            rotateToOriginalPositions(listVariableDescriptor, entity, preShiftAmount);
            for (FlipSublistAction<Solution_> move : equivalent2Opts) {
                move.doMoveOnGenuineVariables();
            }

            if (affectedElementsInfo.getWrappedStartIndex() != -1) {
                innerScoreDirector.afterListVariableChanged(listVariableDescriptor, entity,
                        affectedElementsInfo.getWrappedStartIndex(),
                        listVariableDescriptor.getListSize(entity));
                innerScoreDirector.afterListVariableChanged(listVariableDescriptor, entity, 0,
                        affectedElementsInfo.getWrappedEndIndex());
            }
            for (IntIntImmutablePair affectedInterval : affectedElementsInfo.getAffectedMiddleRangeList()) {
                innerScoreDirector.afterListVariableChanged(listVariableDescriptor, entity, affectedInterval.leftInt(),
                        affectedInterval.rightInt());
            }
        }

        public String toString() {
            return "Undo" + descriptor.toString();
        }
    }

}
