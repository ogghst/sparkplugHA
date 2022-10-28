package it.nm.sparkplugha.events;

import it.nm.sparkplugha.model.SPHADevice;

public class SPHADeviceCommandEvent extends SPHADeviceEvent {

    public SPHADeviceCommandEvent(SPHADevice device) {

	super(device);

    }

}
