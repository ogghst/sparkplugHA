package it.nm.sparkplugha.events;

import java.util.Date;

import it.nm.sparkplugha.SPHANode;

public abstract class SPHANodeEvent implements SPHAEvent {

    private SPHANode node;

    public SPHANode getNode() {

	return node;

    }

    public SPHANodeEvent(SPHANode node) {

	this.node = node;

    }

    @Override
    public Date getTimestamp() {

	return node.getPayload() == null ? new Date() : node.getPayload().getTimestamp() == null? new Date() : node.getPayload().getTimestamp();

    }

    @Override
    public String toString() {

	return node == null ? "<no descriptor>" : node.toString();

    }

}
