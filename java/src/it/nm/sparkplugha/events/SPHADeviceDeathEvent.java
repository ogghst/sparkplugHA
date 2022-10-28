package it.nm.sparkplugha.events;

import it.nm.sparkplugha.model.SPHADevice;

public class SPHADeviceDeathEvent extends SPHADeviceEvent {

    public SPHADeviceDeathEvent(SPHADevice device) {

	super(device);

    }

}
