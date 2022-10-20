package it.nm.sparkplugha.model;

import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Topic;

public abstract class SPHAFeatureLocal extends SPHAFeature {

    public SPHAFeatureLocal(String name, SPHANodeLocal node, SparkplugBPayload payload) {

	super(name, node, payload);

    }

    public SPHANodeLocal getNode() {

	return (SPHANodeLocal) super.getNode();

    }

    public abstract void DataArrived(EdgeNodeDescriptor node, Metric metric) throws Exception;

    public abstract void CommandArrived(EdgeNodeDescriptor node, Metric metric) throws Exception;

}
