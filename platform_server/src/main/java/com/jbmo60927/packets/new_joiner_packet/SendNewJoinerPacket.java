package com.jbmo60927.packets.new_joiner_packet;

import com.jbmo60927.packets.SendPacket;

public class SendNewJoinerPacket extends SendPacket implements NewJoinerPacket {

    public SendNewJoinerPacket() {
        super(PacketType.NEWJOINER, new byte[0]);
    }
}