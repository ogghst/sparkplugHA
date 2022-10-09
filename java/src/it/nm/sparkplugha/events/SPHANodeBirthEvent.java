package it.nm.sparkplugha.events;

import java.util.Date;

import org.eclipse.tahu.message.model.SparkplugBPayload;

import it.nm.sparkplugha.model.SPHAEdgeNodeDescriptor;

public class SPHANodeBirthEvent implements SPHAEvent {

    private SPHAEdgeNodeDescriptor nodeDesc;
    private SparkplugBPayload payload;

    public SPHAEdgeNodeDescriptor getNodeDesc() {

	return nodeDesc;

    }


    public SPHANodeBirthEvent(SPHAEdgeNodeDescriptor nodeDesc, SparkplugBPayload payload) {

	this.nodeDesc = nodeDesc;
	this.payload = payload;

    }


    public SparkplugBPayload getPayload() {

	return payload;

    }


    @Override
    public Date getTimestamp() {

	return payload == null? new Date(): payload.getTimestamp();

    }

}
