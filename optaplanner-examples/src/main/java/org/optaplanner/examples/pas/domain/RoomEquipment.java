package org.optaplanner.examples.pas.domain;

import org.optaplanner.examples.common.domain.AbstractPersistableJackson;
import org.optaplanner.examples.common.persistence.jackson.JacksonUniqueIdGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class RoomEquipment extends AbstractPersistableJackson {

    private Room room;
    private Equipment equipment;

    public RoomEquipment() { // For Jackson.
    }

    public RoomEquipment(long id, Room room, Equipment equipment) {
        super(id);
        this.room = room;
        this.equipment = equipment;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    @Override
    public String toString() {
        return room + "-" + equipment;
    }

}
