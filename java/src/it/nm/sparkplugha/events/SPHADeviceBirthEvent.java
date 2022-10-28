package it.nm.sparkplugha.events;

import it.nm.sparkplugha.model.SPHADevice;

public class SPHADeviceBirthEvent extends SPHADeviceEvent {

    public SPHADeviceBirthEvent(SPHADevice device) {

	super(device);

    }

}
