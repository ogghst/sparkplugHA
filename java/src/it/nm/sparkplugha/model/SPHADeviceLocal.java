package it.nm.sparkplugha.model;

import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.Metric;

public abstract class SPHADeviceLocal extends SPHADevice {

    public SPHADeviceLocal(String name, SPHANodeLocal node) {

	super(name, node);

    }

    public SPHANodeLocal getNode() {

	return (SPHANodeLocal) super.getNode();

    }

    public abstract void DataArrived(EdgeNodeDescriptor node, Metric metric) throws Exception;

    public abstract void CommandArrived(EdgeNodeDescriptor node, Metric metric) throws Exception;

}
