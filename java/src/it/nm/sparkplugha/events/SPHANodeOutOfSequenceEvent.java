package it.nm.sparkplugha.events;

import it.nm.sparkplugha.model.SPHANode;

public class SPHANodeOutOfSequenceEvent extends SPHANodeEvent {

    public SPHANodeOutOfSequenceEvent(SPHANode node) {

	super(node);

    }

}
