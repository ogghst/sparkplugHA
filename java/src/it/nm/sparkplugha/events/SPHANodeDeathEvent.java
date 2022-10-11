package it.nm.sparkplugha.events;

import java.util.Date;

import org.eclipse.tahu.message.model.SparkplugBPayload;

import it.nm.sparkplugha.model.SPHAEdgeNode;

public class SPHANodeDeathEvent implements SPHANodeEvent {

    private SPHAEdgeNode node;

    @Override
    public SPHAEdgeNode getNode() {

	return node;

    }

    public SPHANodeDeathEvent(SPHAEdgeNode node ) {

	this.node = node;


    }

    @Override
    public Date getTimestamp() {

	return node.getPayload() == null ? new Date() : node.getPayload().getTimestamp();

    }

    @Override
    public String toString() {

	return node == null ? "<no descriptor>" : node.toString();

    }

}
