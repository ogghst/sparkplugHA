package it.nm.sparkplugha.events;

import it.nm.sparkplugha.model.SPHADevice;

public class SPHADeviceDataEvent extends SPHADeviceEvent {

    public SPHADeviceDataEvent(SPHADevice device) {

	super(device);

    }

}
