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

package org.optaplanner.examples.conferencescheduling.optional.score;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.optaplanner.examples.conferencescheduling.domain.ConferenceConstraintConfiguration;
import org.optaplanner.examples.conferencescheduling.domain.ConferenceSolution;
import org.optaplanner.examples.conferencescheduling.domain.Room;
import org.optaplanner.examples.conferencescheduling.domain.Speaker;
import org.optaplanner.examples.conferencescheduling.domain.Talk;
import org.optaplanner.examples.conferencescheduling.domain.Timeslot;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

public class ConferenceSchedulingConstraintProviderTest {

    private final ConstraintVerifier<ConferenceSchedulingConstraintProvider, ConferenceSolution> constraintVerifier =
            ConstraintVerifier.build(new ConferenceSchedulingConstraintProvider(), ConferenceSolution.class,
                    Talk.class);

    private static final LocalDateTime START = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());

    private static final Timeslot TIMESLOT1 = new Timeslot(1)
            .withStartDateTime(START)
            .withEndDateTime(START.plusHours(1))
            .withTagSet(singleton("a"));
    private static final Timeslot TIMESLOT2 = new Timeslot(2)
            .withStartDateTime(TIMESLOT1.getEndDateTime())
            .withEndDateTime(TIMESLOT1.getEndDateTime().plusHours(1))
            .withTagSet(singleton("b"));
    private static final Timeslot TIMESLOT3 = new Timeslot(3)
            .withStartDateTime(TIMESLOT2.getEndDateTime())
            .withEndDateTime(TIMESLOT2.getEndDateTime().plusHours(1))
            .withTagSet(singleton("c"));
    private static final Timeslot TIMESLOT4 = new Timeslot(3)
            .withStartDateTime(TIMESLOT1.getStartDateTime().plusDays(1))
            .withEndDateTime(TIMESLOT1.getStartDateTime().plusDays(1).plusHours(1))
            .withTagSet(singleton("c"));

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Test
    public void roomUnavailableTimeslot() {
        Room room1 = new Room(1)
                .withUnavailableTimeslotSet(singleton(TIMESLOT1));
        Room room2 = new Room(2)
                .withUnavailableTimeslotSet(singleton(TIMESLOT2));
        Talk talk1 = new Talk(1)
                .withRoom(room1)
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room2)
                .withTimeslot(TIMESLOT1);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::roomUnavailableTimeslot)
                .given(talk1, talk2)
                .penalizesBy(TIMESLOT1.getDurationInMinutes()); // room1 is in an unavailable timeslot.
    }

    @Test
    public void roomConflict() {
        Room room = new Room(1)
                .withUnavailableTimeslotSet(singleton(TIMESLOT1));
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withTimeslot(TIMESLOT1);
        Talk talk3 = new Talk(3)
                .withRoom(room)
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::roomConflict)
                .given(talk1, talk2, talk3)
                .penalizesBy(TIMESLOT1.getDurationInMinutes()); // talk1 and talk2 are in conflict.
    }

    @Test
    public void speakerUnavailableTimeslot() {
        Room room = new Room(0);
        Speaker speaker1 = new Speaker(1)
                .withUnavailableTimeslotSet(singleton(TIMESLOT1));
        Speaker speaker2 = new Speaker(2)
                .withUnavailableTimeslotSet(singleton(TIMESLOT2));
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker1))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker2))
                .withTimeslot(TIMESLOT1);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerUnavailableTimeslot)
                .given(talk1, talk2)
                .penalizesBy(TIMESLOT1.getDurationInMinutes()); // speaker1 is in an unavailable timeslot.
    }

    @Test
    public void speakerConflict() {
        Room room = new Room(0);
        Speaker speaker = new Speaker(1);
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker))
                .withTimeslot(TIMESLOT1);
        Talk talk3 = new Talk(3)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker))
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerConflict)
                .given(speaker, talk1, talk2, talk3)
                .penalizesBy(TIMESLOT1.getDurationInMinutes()); // talk1 and talk2 are in conflict.
    }

    @Test
    public void talkPrerequisiteTalks() {
        Room room = new Room(0);
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withPrerequisiteTalksCodesSet(emptySet())
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withPrerequisiteTalksCodesSet(singleton(talk1))
                .withTimeslot(TIMESLOT1);
        Talk talk3 = new Talk(3)
                .withRoom(room)
                .withPrerequisiteTalksCodesSet(singleton(talk1))
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkPrerequisiteTalks)
                .given(talk1, talk2, talk3)
                .penalizesBy(TIMESLOT1.getDurationInMinutes() * 2); // talk2 is not after talk1.
    }

    @Test
    public void talkMutuallyExclusiveTalksTags() {
        Room room = new Room(0);
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withMutuallyExclusiveTalksTagSet(emptySet())
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withMutuallyExclusiveTalksTagSet(new HashSet<>(Arrays.asList("a", "b")))
                .withTimeslot(TIMESLOT1);
        Talk talk3 = new Talk(3)
                .withRoom(room)
                .withMutuallyExclusiveTalksTagSet(new HashSet<>(Arrays.asList("a", "b", "c")))
                .withTimeslot(TIMESLOT1);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkMutuallyExclusiveTalksTags)
                .given(talk1, talk2, talk3)
                .penalizesBy(TIMESLOT1.getDurationInMinutes() * 2); // talk2 and talk3 excluded twice.
    }

    @Test
    public void consecutiveTalksPause() {
        Room room = new Room(0);
        Speaker speaker1 = new Speaker(1);
        Speaker speaker2 = new Speaker(2);
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker1))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker1))
                .withTimeslot(TIMESLOT2);
        Talk talk3 = new Talk(3)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker1))
                .withTimeslot(TIMESLOT3);
        Talk talk4 = new Talk(4)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker2))
                .withTimeslot(TIMESLOT1);
        ConferenceConstraintConfiguration configuration = new ConferenceConstraintConfiguration(0);
        configuration.setMinimumConsecutiveTalksPauseInMinutes(10);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::consecutiveTalksPause)
                .given(configuration, talk1, talk2, talk3, talk4)
                .penalizesBy(TIMESLOT1.getDurationInMinutes() * 4); // talk1+talk2 , talk2+talk3.
    }

    @Test
    public void crowdControl() {
        Room room = new Room(0);
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withTimeslot(TIMESLOT1);
        talk1.setCrowdControlRisk(1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withTimeslot(TIMESLOT1);
        talk2.setCrowdControlRisk(1);
        Talk talk3 = new Talk(3)
                .withRoom(room)
                .withTimeslot(TIMESLOT1);
        talk3.setCrowdControlRisk(1);
        Talk talk4 = new Talk(4)
                .withRoom(room)
                .withTimeslot(TIMESLOT2);
        talk4.setCrowdControlRisk(1);
        Talk talk5 = new Talk(5)
                .withRoom(room)
                .withTimeslot(TIMESLOT2);
        talk5.setCrowdControlRisk(1);
        Talk noRiskTalk = new Talk(6)
                .withRoom(room)
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::crowdControl)
                .given(talk1, talk2, talk3, talk4, talk5, noRiskTalk)
                .penalizesBy(TIMESLOT1.getDurationInMinutes() * 3); // talk1, talk2, talk3.
    }

    @Test
    public void speakerRequiredTimeslotTags() {
        Room room = new Room(0);
        Speaker speaker1 = new Speaker(1)
                .withRequiredTimeslotTagSet(singleton("a"));
        Speaker speaker2 = new Speaker(1)
                .withRequiredTimeslotTagSet(singleton("x"));
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker1))
                .withRequiredTimeslotTagSet(new HashSet<>(Arrays.asList("a", "b")))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker2))
                .withRequiredTimeslotTagSet(emptySet())
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerRequiredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(TIMESLOT2.getDurationInMinutes());
    }

    @Test
    public void speakerProhibitedTimeslotTags() {
        Room room = new Room(0);
        Speaker speaker1 = new Speaker(1)
                .withProhibitedTimeslotTagSet(singleton("a"));
        Speaker speaker2 = new Speaker(1)
                .withProhibitedTimeslotTagSet(singleton("x"));
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker1))
                .withProhibitedTimeslotTagSet(new HashSet<>(Arrays.asList("a", "b")))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker2))
                .withProhibitedTimeslotTagSet(emptySet())
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerProhibitedTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(TIMESLOT1.getDurationInMinutes());
    }

    @Test
    public void talkRequiredTimeslotTags() {
        Room room = new Room(0);
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withRequiredTimeslotTagSet(new HashSet<>(Arrays.asList("a", "b")))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withRequiredTimeslotTagSet(emptySet())
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkRequiredTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(TIMESLOT2.getDurationInMinutes());
    }

    @Test
    public void talkProhibitedTimeslotTags() {
        Room room = new Room(0);
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withProhibitedTimeslotTagSet(new HashSet<>(Arrays.asList("a", "b")))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withProhibitedTimeslotTagSet(emptySet())
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkProhibitedTimeslotTags)
                .given(talk1, talk2)
                .penalizesBy(TIMESLOT1.getDurationInMinutes());
    }

    @Test
    public void speakerRequiredRoomTags() {
        Room room = new Room(0)
                .withTagSet(singleton("a"));
        Speaker speaker1 = new Speaker(1)
                .withRequiredRoomTagSet(singleton("a"));
        Speaker speaker2 = new Speaker(1)
                .withRequiredRoomTagSet(singleton("x"));
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker1))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker2))
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerRequiredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(TIMESLOT2.getDurationInMinutes());
    }

    @Test
    public void speakerProhibitedRoomTags() {
        Room room = new Room(0)
                .withTagSet(singleton("a"));
        Speaker speaker1 = new Speaker(1)
                .withProhibitedRoomTagSet(singleton("a"));
        Speaker speaker2 = new Speaker(1)
                .withProhibitedRoomTagSet(singleton("x"));
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker1))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withSpeakerList(singletonList(speaker2))
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::speakerProhibitedRoomTags)
                .given(talk1, talk2)
                .penalizesBy(TIMESLOT1.getDurationInMinutes());
    }

    @Test
    public void talkRequiredRoomTags() {
        Room room = new Room(0)
                .withTagSet(singleton("a"));
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withRequiredRoomTagSet(new HashSet<>(Arrays.asList("a", "b")))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withRequiredRoomTagSet(emptySet())
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkRequiredRoomTags)
                .given(talk1, talk2)
                .penalizesBy(TIMESLOT2.getDurationInMinutes());
    }

    @Test
    public void talkProhibitedRoomTags() {
        Room room = new Room(0)
                .withTagSet(singleton("a"));
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withProhibitedRoomTagSet(new HashSet<>(Arrays.asList("a", "b")))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withProhibitedRoomTagSet(emptySet())
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::talkProhibitedRoomTags)
                .given(talk1, talk2)
                .penalizesBy(TIMESLOT1.getDurationInMinutes());
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    @Test
    public void publishedTimeslot() {
        Room room = new Room(0);
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withTimeslot(TIMESLOT1);
        talk1.setPublishedTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withTimeslot(TIMESLOT2);
        talk2.setPublishedTimeslot(TIMESLOT1);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::publishedTimeslot)
                .given(talk1, talk2)
                .penalizesBy(1);
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Test
    public void publishedRoom() {
        Room room1 = new Room(0);
        Room room2 = new Room(1);
        Talk talk1 = new Talk(1)
                .withRoom(room1)
                .withTimeslot(TIMESLOT1);
        talk1.setPublishedRoom(room1);
        Talk talk2 = new Talk(2)
                .withRoom(room1)
                .withTimeslot(TIMESLOT2);
        talk2.setPublishedRoom(room2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::publishedRoom)
                .given(talk1, talk2)
                .penalizesBy(1);
    }

    @Test
    public void themeTrackConflict() {
        Room room = new Room(0);
        Talk talk1 = new Talk(1)
                .withRoom(room)
                .withThemeTrackTagSet(singleton("a"))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room)
                .withThemeTrackTagSet(singleton("a"))
                .withTimeslot(TIMESLOT1);
        Talk talk3 = new Talk(3)
                .withRoom(room)
                .withThemeTrackTagSet(singleton("b"))
                .withTimeslot(TIMESLOT1);
        Talk talk4 = new Talk(3)
                .withRoom(room)
                .withThemeTrackTagSet(singleton("a"))
                .withTimeslot(TIMESLOT2);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::themeTrackConflict)
                .given(talk1, talk2, talk3, talk4)
                .penalizesBy(60); // talk1 + talk2.
    }

    @Test
    public void themeTrackRoomStability() {
        Room room1 = new Room(0);
        Room room2 = new Room(1);
        Talk talk1 = new Talk(1)
                .withRoom(room1)
                .withThemeTrackTagSet(singleton("a"))
                .withTimeslot(TIMESLOT1);
        Talk talk2 = new Talk(2)
                .withRoom(room2)
                .withThemeTrackTagSet(singleton("a"))
                .withTimeslot(TIMESLOT2);
        Talk talk3 = new Talk(3)
                .withRoom(room1)
                .withThemeTrackTagSet(singleton("b"))
                .withTimeslot(TIMESLOT3);
        Talk talk4 = new Talk(4)
                .withRoom(room2)
                .withThemeTrackTagSet(singleton("a"))
                .withTimeslot(TIMESLOT4);

        constraintVerifier.verifyThat(ConferenceSchedulingConstraintProvider::themeTrackRoomStability)
                .given(talk1, talk2, talk3, talk4)
                .penalizesBy(120); // talk1 + talk2.
    }

}
