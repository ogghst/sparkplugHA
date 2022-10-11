package it.nm.sparkplugha.events;

import java.util.Date;

import org.eclipse.tahu.message.model.SparkplugBPayload;

import it.nm.sparkplugha.model.SPHAEdgeNode;

public class SPHANodeOutOfSequenceEvent implements SPHAEvent {

    private Date date;
    private SPHAEdgeNode node;

    public SPHANodeOutOfSequenceEvent(SPHAEdgeNode node) {

	this.node = node;
	date = new Date();

    }

    @Override
    public Date getTimestamp() {

	return date;

    }

    public SPHAEdgeNode getNode() {

	return node;

    }

    @Override
    public String toString() {

	return node == null ? "<no descriptor>" : node.toString();

    }

}
