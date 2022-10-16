package it.nm.sparkplugha.events;

import java.util.Date;

import it.nm.sparkplugha.SPHANode;

public class SPHANodeOutOfSequenceEvent extends SPHANodeEvent {

    public SPHANodeOutOfSequenceEvent(SPHANode node) {

	super(node);

    }

}
