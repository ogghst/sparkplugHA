package it.nm.sparkplugha.model;

import org.eclipse.tahu.message.model.EdgeNodeDescriptor;

public class SPHAEdgeNodeDescriptor extends EdgeNodeDescriptor {

    /*
     * public SPHAEdgeNodeDescriptor(String descriptorString) {
     * 
     * super(descriptorString);
     * 
     * }
     */

    public SPHAEdgeNodeDescriptor(String groupId, String edgeNodeId) {

	super(groupId + "/" + edgeNodeId);

    }

    private SPHANodeState state;

    public enum SPHANodeState {
	UNINITIALIZED, INIT, ONLINE, OFFLINE, ERROR,
    }

    private long lastSeqNumber;

    public long getLastSeqNumber() {

	return lastSeqNumber;

    }

    public void setLastSeqNumber(long lastSeqNumber) {

	this.lastSeqNumber = lastSeqNumber;

    }

    @Override
    public int hashCode() {

	final int prime = 31;
	int result = 1;
	result = prime * result + super.hashCode();
	return result;

    }

    @Override
    public boolean equals(Object obj) {

	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	SPHAEdgeNodeDescriptor other = (SPHAEdgeNodeDescriptor) obj;

	if (!super.equals(other))
	    return false;

	return (other.getState() == getState() && other.getLastSeqNumber() == getLastSeqNumber());

    }

    public SPHANodeState getState() {

	return state;

    }

    public void setState(SPHANodeState state) {

	this.state = state;

    }

}
