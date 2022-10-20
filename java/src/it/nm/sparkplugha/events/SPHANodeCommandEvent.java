package it.nm.sparkplugha.events;

import it.nm.sparkplugha.model.SPHANode;

public class SPHANodeCommandEvent extends SPHANodeEvent {

    public SPHANodeCommandEvent(SPHANode node) {

	super(node);

    }

}
