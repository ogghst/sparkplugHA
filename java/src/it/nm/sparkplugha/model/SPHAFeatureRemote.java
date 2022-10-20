package it.nm.sparkplugha.model;

import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Topic;

public abstract class SPHAFeatureRemote extends SPHAFeature {

    public SPHAFeatureRemote(String name, SPHANodeRemote node, SparkplugBPayload payload) {

	super(name, node, payload);

    }

    public SPHANodeRemote getNode() {

	return (SPHANodeRemote) super.getNode();

    }

}
