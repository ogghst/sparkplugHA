package it.nm.sparkplugha.events;

import it.nm.sparkplugha.model.SPHANode;

public class SPHADeviceBirthEvent extends SPHANodeEvent {

    public SPHADeviceBirthEvent(SPHANode node) {

	super(node);

    }

}
