package it.nm.sparkplugha.events;

import it.nm.sparkplugha.model.SPHANode;

public class SPHADeviceCommandEvent extends SPHANodeEvent {

    public SPHADeviceCommandEvent(SPHANode node) {

	super(node);

    }

}
