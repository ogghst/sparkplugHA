package it.nm.sparkplugha.events;

import it.nm.sparkplugha.SPHANode;

public class SPHANodeDeathEvent extends SPHANodeEvent {

    public SPHANodeDeathEvent(SPHANode node) {

	super(node);

    }

}
