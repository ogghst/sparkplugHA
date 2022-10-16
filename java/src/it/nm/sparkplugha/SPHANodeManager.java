package it.nm.sparkplugha;

import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.SparkplugBPayload;

import it.nm.sparkplugha.model.SPHAFeature;

public class SPHANodeManager {

    public SPHANodeManager() {

	// TODO Auto-generated constructor stub

    }

    public abstract void publishNodeData(SparkplugBPayload payload) throws Exception;

    public abstract void publishNodeCommand(SPHANode descriptor, SparkplugBPayload payload) throws Exception;

    public abstract void publishFeatureData(SPHAFeature feature, SparkplugBPayload payload) throws Exception;

    public abstract void publishFeatureCommand(SPHAFeature feature, EdgeNodeDescriptor descriptor,
    SparkplugBPayload payload) throws Exception;

}
