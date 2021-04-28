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

package org.optaplanner.examples.nurserostering.optional.score;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.core.impl.util.ConsecutiveData;
import org.optaplanner.core.impl.util.Sequence;
import org.optaplanner.examples.nurserostering.domain.Employee;
import org.optaplanner.examples.nurserostering.domain.ShiftAssignment;
import org.optaplanner.examples.nurserostering.domain.contract.ContractLineType;
import org.optaplanner.examples.nurserostering.domain.contract.MinMaxContractLine;

public class NurseRosteringConstraintProvider implements ConstraintProvider {

    public BiConstraintStream<ImmutablePair<Employee, MinMaxContractLine>, Sequence<ShiftAssignment>>
            getConsecutiveShifts(UniConstraintStream<MinMaxContractLine> constraintStream) {
        return constraintStream.join(Employee.class, Joiners.equal(MinMaxContractLine::getContract, Employee::getContract))
                .join(ShiftAssignment.class, Joiners.equal((contract, employee) -> employee, ShiftAssignment::getEmployee))
                .groupBy((contract, employee, shift) -> ImmutablePair.of(employee, contract),
                        ConstraintCollectors.consecutive((contract, employee, shift) -> shift,
                                ShiftAssignment::getShiftDateDayIndex))
                .flattenLast(ConsecutiveData::getConsecutiveSequences);
    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                oneShiftPerDay(constraintFactory),
                minimumAndMaximumNumberOfAssignments(constraintFactory),
                minimumConsecutiveWorkingDays(constraintFactory),
                maximumConsecutiveWorkingDays(constraintFactory)
        };
    }

    // ############################################################################
    // Hard constraints
    // ############################################################################

    // A nurse can only work one shift per day, i.e. no two shift can be assigned to the same nurse on a day.
    Constraint oneShiftPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory
                .fromUniquePair(ShiftAssignment.class,
                        Joiners.equal(ShiftAssignment::getEmployee, ShiftAssignment::getShiftDate))
                .penalize("oneShiftPerDay", HardSoftScore.ONE_HARD);
    }

    // ############################################################################
    // Soft constraints
    // ############################################################################
    Constraint minimumAndMaximumNumberOfAssignments(ConstraintFactory constraintFactory) {
        return constraintFactory.from(MinMaxContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine.getContractLineType() == ContractLineType.TOTAL_ASSIGNMENTS &&
                        minMaxContractLine.isEnabled())
                .join(Employee.class, Joiners.equal(MinMaxContractLine::getContract, Employee::getContract))
                .join(ShiftAssignment.class, Joiners.equal((contractLine, employee) -> employee, ShiftAssignment::getEmployee))
                .groupBy((line, employee, shift) -> ImmutablePair.of(employee, line), ConstraintCollectors.countTri())
                .filter((employeeContractPair, shiftCount) -> employeeContractPair.getRight().isViolated(shiftCount))
                .penalize("Minimum and maximum number of assignments", HardSoftScore.ONE_SOFT,
                        (employeeContractPair, shiftCount) -> employeeContractPair.getRight().getViolationAmount(shiftCount));
    }

    Constraint minimumConsecutiveWorkingDays(ConstraintFactory constraintFactory) {
        return getConsecutiveShifts(constraintFactory.from(MinMaxContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine
                        .getContractLineType() == ContractLineType.CONSECUTIVE_WORKING_DAYS &&
                        minMaxContractLine.isMinimumEnabled()))
                                .filter((employeeContractPair,
                                        shiftList) -> shiftList.getLength() < employeeContractPair.getRight().getMinimumValue())
                                .penalize("minimumConsecutiveWorkingDays", HardSoftScore.ONE_SOFT,
                                        (employeeContractPair, shiftList) -> employeeContractPair.getRight()
                                                .getViolationAmount(shiftList.getLength()));

    }

    Constraint maximumConsecutiveWorkingDays(ConstraintFactory constraintFactory) {
        return getConsecutiveShifts(constraintFactory.from(MinMaxContractLine.class)
                .filter(minMaxContractLine -> minMaxContractLine
                        .getContractLineType() == ContractLineType.CONSECUTIVE_WORKING_DAYS &&
                        minMaxContractLine.isMaximumEnabled()))
                                .filter((employeeContractPair,
                                        shiftList) -> shiftList.getLength() > employeeContractPair.getRight().getMaximumValue())
                                .penalize("maximumConsecutiveWorkingDays", HardSoftScore.ONE_SOFT,
                                        (employeeContractPair, shiftList) -> employeeContractPair.getRight()
                                                .getViolationAmount(shiftList.getLength()));

    }

    /*
     * rule "insertEmployeeFreeSequence"
     * salience 1 // Do these rules first (optional, for performance)
     * when
     * EmployeeConsecutiveAssignmentEnd(
     * $employee : employee,
     * $firstDayIndexMinusOne : shiftDateDayIndex
     * )
     * 
     * EmployeeConsecutiveAssignmentStart(
     * employee == $employee,
     * shiftDateDayIndex > $firstDayIndexMinusOne,
     * $lastDayIndexPlusOne : shiftDateDayIndex
     * )
     * 
     * // There are no working days between the first and last day
     * not EmployeeConsecutiveAssignmentStart(
     * employee == $employee,
     * shiftDateDayIndex > $firstDayIndexMinusOne && < $lastDayIndexPlusOne
     * )
     * then
     * insertLogical(new EmployeeFreeSequence($employee, $firstDayIndexMinusOne + 1, $lastDayIndexPlusOne - 1));
     * end
     * rule "insertFirstEmployeeFreeSequence"
     * salience 1 // Do these rules first (optional, for performance)
     * when
     * EmployeeConsecutiveAssignmentStart(
     * $employee : employee,
     * $lastDayIndexPlusOne : shiftDateDayIndex
     * )
     * 
     * // There are no working days before the first day
     * not EmployeeConsecutiveAssignmentEnd(
     * employee == $employee,
     * shiftDateDayIndex < $lastDayIndexPlusOne
     * )
     * NurseRosterParametrization(firstShiftDateDayIndex < $lastDayIndexPlusOne, $firstDayIndex : firstShiftDateDayIndex)
     * then
     * insertLogical(new EmployeeFreeSequence($employee, $firstDayIndex, $lastDayIndexPlusOne - 1));
     * end
     * rule "insertLastEmployeeFreeSequence"
     * salience 1 // Do these rules first (optional, for performance)
     * when
     * EmployeeConsecutiveAssignmentEnd(
     * $employee : employee,
     * $firstDayIndexMinusOne : shiftDateDayIndex
     * )
     * 
     * // There are no working days after the last day
     * not EmployeeConsecutiveAssignmentStart(
     * employee == $employee,
     * shiftDateDayIndex > $firstDayIndexMinusOne
     * )
     * NurseRosterParametrization(lastShiftDateDayIndex > $firstDayIndexMinusOne, $lastDayIndex : lastShiftDateDayIndex)
     * then
     * insertLogical(new EmployeeFreeSequence($employee, $firstDayIndexMinusOne + 1, $lastDayIndex));
     * end
     * rule "insertEntireEmployeeFreeSequence"
     * salience 1 // Do these rules first (optional, for performance)
     * when
     * $employee : Employee()
     * // There are no working days at all
     * not EmployeeConsecutiveAssignmentStart(
     * employee == $employee
     * )
     * NurseRosterParametrization($firstDayIndex : firstShiftDateDayIndex, $lastDayIndex : lastShiftDateDayIndex)
     * then
     * insertLogical(new EmployeeFreeSequence($employee, $firstDayIndex, $lastDayIndex));
     * end
     * 
     * // Minimum number of consecutive free days
     * rule "minimumConsecutiveFreeDays"
     * when
     * $contractLine : MinMaxContractLine(
     * contractLineType == ContractLineType.CONSECUTIVE_FREE_DAYS, minimumEnabled == true,
     * $contract : contract, $minimumValue : minimumValue
     * )
     * 
     * EmployeeFreeSequence(
     * getEmployee().getContract() == $contract,
     * dayLength < $minimumValue,
     * $dayLength : dayLength
     * )
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext, ($dayLength - $minimumValue) * $contractLine.getMinimumWeight());
     * end
     * 
     * // Maximum number of consecutive free days
     * rule "maximumConsecutiveFreeDays"
     * when
     * $contractLine : MinMaxContractLine(
     * contractLineType == ContractLineType.CONSECUTIVE_FREE_DAYS, maximumEnabled == true,
     * $contract : contract, $maximumValue : maximumValue
     * )
     * 
     * EmployeeFreeSequence(
     * getEmployee().getContract() == $contract,
     * dayLength > $maximumValue,
     * $dayLength : dayLength
     * )
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext, ($maximumValue - $dayLength) * $contractLine.getMaximumWeight());
     * end
     * 
     * 
     * rule "insertEmployeeConsecutiveWeekendAssignmentStart"
     * salience 2 // Do these rules first (optional, for performance)
     * when
     * ShiftAssignment(
     * weekend == true,
     * $employee : employee, employee != null,
     * $weekendSundayIndex : weekendSundayIndex
     * )
     * // The first working weekend has no working weekend before it
     * not ShiftAssignment(
     * weekend == true,
     * employee == $employee,
     * weekendSundayIndex == ($weekendSundayIndex - 7)
     * )
     * then
     * insertLogical(new EmployeeConsecutiveWeekendAssignmentStart($employee, $weekendSundayIndex));
     * end
     * rule "insertEmployeeConsecutiveWeekendAssignmentEnd"
     * salience 2 // Do these rules first (optional, for performance)
     * when
     * ShiftAssignment(
     * weekend == true,
     * $employee : employee, employee != null,
     * $weekendSundayIndex : weekendSundayIndex
     * )
     * // The last working weekend has no working weekend after it
     * not ShiftAssignment(
     * weekend == true,
     * employee == $employee,
     * weekendSundayIndex == ($weekendSundayIndex + 7)
     * )
     * then
     * insertLogical(new EmployeeConsecutiveWeekendAssignmentEnd($employee, $weekendSundayIndex));
     * end
     * 
     * rule "insertEmployeeWeekendSequence"
     * when
     * EmployeeConsecutiveWeekendAssignmentStart(
     * $employee : employee,
     * $firstSundayIndex : sundayIndex
     * )
     * 
     * EmployeeConsecutiveWeekendAssignmentEnd(
     * employee == $employee,
     * sundayIndex >= $firstSundayIndex,
     * $lastSundayIndex : sundayIndex
     * )
     * 
     * // There are no free weekends between the first and last weekend
     * not EmployeeConsecutiveWeekendAssignmentEnd(
     * employee == $employee,
     * sundayIndex >= $firstSundayIndex && < $lastSundayIndex
     * )
     * then
     * insertLogical(new EmployeeWeekendSequence($employee, $firstSundayIndex, $lastSundayIndex));
     * end
     * 
     * // Minimum number of consecutive working weekends
     * rule "minimumConsecutiveWorkingWeekends"
     * when
     * $contractLine : MinMaxContractLine(
     * contractLineType == ContractLineType.CONSECUTIVE_WORKING_WEEKENDS, minimumEnabled == true,
     * $contract : contract, $minimumValue : minimumValue
     * )
     * 
     * EmployeeWeekendSequence(
     * getEmployee().getContract() == $contract,
     * weekendLength < $minimumValue,
     * $weekendLength : weekendLength
     * )
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext,
     * ($weekendLength - $minimumValue) * $contractLine.getMinimumWeight());
     * end
     * 
     * // Maximum number of consecutive working weekends
     * rule "maximumConsecutiveWorkingWeekends"
     * when
     * $contractLine : MinMaxContractLine(
     * contractLineType == ContractLineType.CONSECUTIVE_WORKING_WEEKENDS, maximumEnabled == true,
     * $contract : contract, $maximumValue : maximumValue
     * )
     * 
     * EmployeeWeekendSequence(
     * getEmployee().getContract() == $contract,
     * weekendLength > $maximumValue,
     * $weekendLength : weekendLength
     * )
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext,
     * ($maximumValue - $weekendLength) * $contractLine.getMaximumWeight());
     * end
     * 
     * 
     * // Complete weekends
     * rule "startOnNotFirstDayOfWeekend"
     * when
     * $contractLine : BooleanContractLine(
     * contractLineType == ContractLineType.COMPLETE_WEEKENDS, enabled == true,
     * $contract : contract
     * )
     * EmployeeConsecutiveAssignmentStart(
     * weekendAndNotFirstDayOfWeekend == true,
     * contract == $contract,
     * $distanceToFirstDayOfWeekend : distanceToFirstDayOfWeekend
     * )
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext, - $distanceToFirstDayOfWeekend * $contractLine.getWeight());
     * end
     * rule "endOnNotLastDayOfWeekend"
     * when
     * $contractLine : BooleanContractLine(
     * contractLineType == ContractLineType.COMPLETE_WEEKENDS, enabled == true,
     * $contract : contract
     * )
     * EmployeeConsecutiveAssignmentEnd(
     * weekendAndNotLastDayOfWeekend == true,
     * contract == $contract,
     * $distanceToLastDayOfWeekend : distanceToLastDayOfWeekend
     * )
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext, - $distanceToLastDayOfWeekend * $contractLine.getWeight());
     * end
     * 
     * // Identical shiftTypes during a weekend
     * rule "identicalShiftTypesDuringWeekend"
     * when
     * $contractLine : BooleanContractLine(contractLineType == ContractLineType.IDENTICAL_SHIFT_TYPES_DURING_WEEKEND,
     * enabled == true, $contract : contract)
     * $employee : Employee(contract == $contract, $weekendLength : weekendLength)
     * ShiftDate(dayOfWeek == DayOfWeek.SUNDAY, $sundayIndex : dayIndex)
     * $shiftType : ShiftType()
     * accumulate(
     * $assignment : ShiftAssignment(
     * weekend == true,
     * weekendSundayIndex == $sundayIndex,
     * employee == $employee,
     * shiftType == $shiftType);
     * $weekendAssignmentTotal : count($assignment);
     * $weekendAssignmentTotal > 0 && $weekendAssignmentTotal < $weekendLength
     * )
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext,
     * ($weekendAssignmentTotal.intValue() - $weekendLength) * $contractLine.getWeight());
     * end
     * 
     * // Requested day on/off
     * rule "dayOffRequest"
     * when
     * DayOffRequest($employee : employee, $shiftDate : shiftDate, $weight : weight)
     * ShiftAssignment(employee == $employee, shiftDate == $shiftDate)
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext, - $weight);
     * end
     * rule "dayOnRequest"
     * when
     * DayOnRequest($employee : employee, $shiftDate : shiftDate, $weight : weight)
     * not ShiftAssignment(employee == $employee, shiftDate == $shiftDate)
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext, - $weight);
     * end
     * 
     * // Requested shift on/off
     * rule "shiftOffRequest"
     * when
     * ShiftOffRequest($employee : employee, $shift : shift, $weight : weight)
     * ShiftAssignment(employee == $employee, shift == $shift)
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext, - $weight);
     * end
     * rule "shiftOnRequest"
     * when
     * ShiftOnRequest($employee : employee, $shift : shift, $weight : weight)
     * not ShiftAssignment(employee == $employee, shift == $shift)
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext, - $weight);
     * end
     * 
     * // Alternative skill
     * rule "alternativeSkill"
     * when
     * $contractLine : BooleanContractLine(contractLineType == ContractLineType.ALTERNATIVE_SKILL_CATEGORY,
     * $contract : contract)
     * ShiftAssignment(contract == $contract, $employee : employee, $shiftType : shiftType)
     * ShiftTypeSkillRequirement(shiftType == $shiftType, $skill : skill)
     * not SkillProficiency(employee == $employee, skill == $skill)
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext, - $contractLine.getWeight());
     * end
     * 
     * // Unwanted patterns
     * rule "unwantedPatternFreeBefore2DaysWithAWorkDayPattern"
     * when
     * $pattern : FreeBefore2DaysWithAWorkDayPattern(
     * $freeDayOfWeek : freeDayOfWeek
     * )
     * PatternContractLine(
     * pattern == $pattern, $contract : contract
     * )
     * ShiftDate(dayOfWeek == $freeDayOfWeek, $freeDayIndex : dayIndex)
     * $employee : Employee(contract == $contract)
     * 
     * not ShiftAssignment(
     * employee == $employee,
     * shiftDateDayIndex == $freeDayIndex
     * )
     * exists ShiftAssignment(
     * employee == $employee,
     * shiftDateDayIndex == ($freeDayIndex + 1) || shiftDateDayIndex == ($freeDayIndex + 2)
     * )
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext, - $pattern.getWeight());
     * end
     * 
     * // TODO support WorkBeforeFreeSequencePattern too (not needed for competition)
     * //rule "unwantedPatternWorkBeforeFreeSequencePattern"
     * // when
     * // $pattern : WorkBeforeFreeSequencePattern(
     * // $workDayOfWeek : workDayOfWeek,
     * // $workShiftType : workShiftType,
     * // $freeDayLength : freeDayLength
     * // )
     * // PatternContractLine(
     * // pattern == $pattern, $contract : contract
     * // )
     * //
     * // ShiftAssignment(
     * // ($workDayOfWeek == null) || (shiftDateDayOfWeek == $workDayOfWeek),
     * // ($workShiftType == null) || (shiftType == $workShiftType),
     * // contract == $contract,
     * // $employee : employee, $workDayIndex : shiftDateDayIndex
     * // )
     * // EmployeeFreeSequence(
     * // employee == $employee,
     * // firstDayIndex == ($workDayIndex + 1),
     * // dayLength >= $freeDayLength
     * // )
     * // then
     * // scoreHolder.addSoftConstraintMatch(kcontext, - $pattern.getWeight());
     * //end
     * rule "unwantedPatternShiftType2DaysPattern"
     * when
     * $pattern : ShiftType2DaysPattern(
     * $dayIndex0ShiftType : dayIndex0ShiftType,
     * $dayIndex1ShiftType : dayIndex1ShiftType
     * )
     * PatternContractLine(
     * pattern == $pattern, $contract : contract
     * )
     * 
     * ShiftAssignment(
     * shiftType == $dayIndex0ShiftType,
     * contract == $contract,
     * $employee : employee, $firstDayIndex : shiftDateDayIndex
     * )
     * ShiftAssignment(
     * ($dayIndex1ShiftType == null) || (shiftType == $dayIndex1ShiftType),
     * employee == $employee,
     * shiftDateDayIndex == ($firstDayIndex + 1)
     * )
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext, - $pattern.getWeight());
     * end
     * rule "unwantedPatternShiftType3DaysPattern"
     * when
     * $pattern : ShiftType3DaysPattern(
     * $dayIndex0ShiftType : dayIndex0ShiftType,
     * $dayIndex1ShiftType : dayIndex1ShiftType,
     * $dayIndex2ShiftType : dayIndex2ShiftType
     * )
     * PatternContractLine(
     * pattern == $pattern, $contract : contract
     * )
     * 
     * ShiftAssignment(
     * shiftType == $dayIndex0ShiftType,
     * contract == $contract,
     * $employee : employee, $firstDayIndex : shiftDateDayIndex
     * )
     * ShiftAssignment(
     * shiftType == $dayIndex1ShiftType,
     * employee == $employee,
     * shiftDateDayIndex == ($firstDayIndex + 1)
     * )
     * ShiftAssignment(
     * shiftType == $dayIndex2ShiftType,
     * employee == $employee,
     * shiftDateDayIndex == ($firstDayIndex + 2)
     * )
     * then
     * scoreHolder.addSoftConstraintMatch(kcontext, - $pattern.getWeight());
     * end
     */

}
