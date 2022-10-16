package it.nm.sparkplugha;

import org.eclipse.tahu.message.model.SparkplugBPayload;


public class SPHANodeRemote extends SPHANode {

    public SPHANodeRemote(String groupId, String edgeNodeId, SPHANodeState state, SparkplugBPayload payload) {

	super(groupId, edgeNodeId, state, payload);


    }

}
