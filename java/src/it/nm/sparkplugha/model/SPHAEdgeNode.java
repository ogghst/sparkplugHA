package it.nm.sparkplugha.model;

import java.util.logging.Logger;

import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.SparkplugBPayload;

import it.nm.sparkplugha.example.simple.SparkPlugMonitor;

public class SPHAEdgeNode extends EdgeNodeDescriptor {

    private final static Logger LOGGER = Logger.getLogger(SparkPlugMonitor.class.getName());

    public enum SPHANodeState {
	OFFLINE, ONLINE
    }

    private long lastSeqNumber;

    private SPHANodeState state;
    private SparkplugBPayload payload;

    public SPHAEdgeNode(String groupId, String edgeNodeId, SPHANodeState state, SparkplugBPayload payload) {

	super(groupId, edgeNodeId);
	lastSeqNumber = 0;
	this.state = state;
	this.payload = payload;

    }

    public long getLastSeqNumber() {

	return lastSeqNumber;

    }

    public SPHANodeState getState() {

	return state;

    }

    public void setLastSeqNumber(long lastSeqNumber) {

	this.lastSeqNumber = lastSeqNumber;

    }

    public void setState(SPHANodeState state) {

	LOGGER.fine("State: " + state);
	this.state = state;

    }

    public SparkplugBPayload getPayload() {

	return payload;

    }

    public void setPayload(SparkplugBPayload payload) {

	this.payload = payload;

    }

}
