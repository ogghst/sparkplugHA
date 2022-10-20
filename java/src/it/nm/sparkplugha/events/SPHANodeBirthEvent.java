package it.nm.sparkplugha.events;

import it.nm.sparkplugha.model.SPHANode;

public class SPHANodeBirthEvent extends SPHANodeEvent {

    public SPHANodeBirthEvent(SPHANode node) {

	super(node);

    }

}
