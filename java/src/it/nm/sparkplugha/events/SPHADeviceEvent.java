package it.nm.sparkplugha.events;

import it.nm.sparkplugha.model.SPHADevice;

public class SPHADeviceEvent implements SPHAEvent {

    public SPHADeviceEvent(SPHADevice device) {

	this.device = device;

    }

    private SPHADevice device;

    public SPHADevice getDevice() {

	return device;

    }

    @Override
    public String toString() {

	return device == null ? "<no descriptor>" : device.toString();

    }

}
