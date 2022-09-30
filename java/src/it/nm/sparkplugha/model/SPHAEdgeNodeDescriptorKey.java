package it.nm.sparkplugha.model;

public class SPHAEdgeNodeDescriptorKey {

    private String groupId;
    private String edgeNodeId;

    public SPHAEdgeNodeDescriptorKey(String groupId, String edgeNodeId) {

	this.edgeNodeId = edgeNodeId;
	this.groupId = groupId;

    }

    public String getGroupId() {

	return groupId;

    }

    public void setGroupId(String groupId) {

	this.groupId = groupId;

    }

    public String getEdgeNodeId() {

	return edgeNodeId;

    }

    public void setEdgeNodeId(String edgeNodeId) {

	this.edgeNodeId = edgeNodeId;

    }

    @Override
    public boolean equals(Object obj) {

	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	SPHAEdgeNodeDescriptorKey other = (SPHAEdgeNodeDescriptorKey) obj;

	return (other.getGroupId() == getGroupId() && other.getEdgeNodeId() == getEdgeNodeId());

    }

    @Override
    public int hashCode() {

	int g = (groupId == null ? 0 : groupId.hashCode());
	int e = (edgeNodeId == null ? 0 : edgeNodeId.hashCode());

	final int prime = 31;
	int result = 1;
	result = prime * result + g + e;
	return result;

    }

}
