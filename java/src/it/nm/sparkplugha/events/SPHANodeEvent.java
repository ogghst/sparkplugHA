package it.nm.sparkplugha.events;

import java.util.Date;

import it.nm.sparkplugha.model.SPHAEdgeNode;

public interface SPHANodeEvent extends SPHAEvent{

    public SPHAEdgeNode getNode();

}
