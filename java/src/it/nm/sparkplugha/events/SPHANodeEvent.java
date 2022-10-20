package it.nm.sparkplugha.events;

import it.nm.sparkplugha.model.SPHANode;

public abstract class SPHANodeEvent implements SPHAEvent {

    private SPHANode node;

    public SPHANode getNode() {

	return node;

    }

    public SPHANodeEvent(SPHANode node) {

	this.node = node;

    }

    @Override
    public String toString() {

	return node == null ? "<no descriptor>" : node.toString();

    }

}
