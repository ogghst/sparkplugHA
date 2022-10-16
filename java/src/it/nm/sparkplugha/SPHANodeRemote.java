package it.nm.sparkplugha;

import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.SparkplugBPayload;

import it.nm.sparkplugha.model.SPHAFeature;


public class SPHANodeRemote extends SPHANode {

    public SPHANodeRemote(String groupId, String edgeNodeId, SPHANodeState state, SparkplugBPayload payload) {

	super(groupId, edgeNodeId, state, payload);
	setSeq(payload.getSeq());
    }

    @Override
    public void publishNodeData(SparkplugBPayload payload) throws Exception {

	// TODO Auto-generated method stub

    }

    @Override
    public void publishNodeCommand(SPHANode descriptor, SparkplugBPayload payload) throws Exception {

	// TODO Auto-generated method stub

    }

    @Override
    public void publishFeatureData(SPHAFeature feature, SparkplugBPayload payload) throws Exception {

	// TODO Auto-generated method stub

    }

    @Override
    public void publishFeatureCommand(SPHAFeature feature, EdgeNodeDescriptor descriptor, SparkplugBPayload payload)
	    throws Exception {

	// TODO Auto-generated method stub

    }

}
