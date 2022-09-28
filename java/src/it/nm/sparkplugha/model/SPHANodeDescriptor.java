package it.nm.sparkplugha.model;

import org.eclipse.tahu.message.model.EdgeNodeDescriptor;

public class SPHANodeDescriptor {

	private String groupName;
	private String edgeNodeName;
	private EdgeNodeDescriptor edgeNodeDescriptor;
	private SPHANodeState state;
	
	public enum SPHANodeState {
		UNINITIALIZED,
		INIT,
		ONLINE,
		OFFLINE,
		ERROR,
	}
	
	private long lastSeqNumber;

	public SPHANodeDescriptor(String groupName, String edgeNodeName) {
		this.groupName = groupName;
		this.edgeNodeName = edgeNodeName;
		this.edgeNodeDescriptor = new EdgeNodeDescriptor(groupName, edgeNodeName);
		this.setState(SPHANodeState.UNINITIALIZED);
		this.lastSeqNumber = 255;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getEdgeNodeName() {
		return edgeNodeName;
	}

	public EdgeNodeDescriptor getEdgeNodeId() {
		return edgeNodeDescriptor;
	}

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
		result = prime * result + ((edgeNodeDescriptor == null) ? 0 : edgeNodeDescriptor.hashCode());
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
		SPHANodeDescriptor other = (SPHANodeDescriptor) obj;
		if (edgeNodeDescriptor == null) {
			if (other.edgeNodeDescriptor != null)
				return false;
		} else if (!edgeNodeDescriptor.equals(other.edgeNodeDescriptor))
			return false;
		return true;
	}

	public SPHANodeState getState() {
		return state;
	}

	public void setState(SPHANodeState state) {
		this.state = state;
	}
}
