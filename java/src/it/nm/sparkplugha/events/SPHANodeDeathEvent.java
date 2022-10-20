package it.nm.sparkplugha.events;

import it.nm.sparkplugha.model.SPHANode;

public class SPHANodeDeathEvent extends SPHANodeEvent {

    public SPHANodeDeathEvent(SPHANode node) {

	super(node);

    }

}
