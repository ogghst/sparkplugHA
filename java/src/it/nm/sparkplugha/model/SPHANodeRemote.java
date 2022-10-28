package it.nm.sparkplugha.model;

import java.util.logging.Logger;

import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Topic;

public class SPHANodeRemote extends SPHANode {

    private final static Logger LOGGER = Logger.getLogger(SPHANodeRemote.class.getName());

    // TODO remove
    private SparkplugBPayload payload;

    public SPHANodeRemote(Topic topic, SPHANodeState state, SparkplugBPayload payload) {

	super(topic.getGroupId(), topic.getEdgeNodeId(), state, payload);
	// buildFeatures(topic, payload);
	// buildMetrics(topic, payload);

    }

    public SPHADeviceRemote addDevice(Topic topic, SparkplugBPayload payload) {

	if (topic == null || topic.getDeviceId() == null) {

	    LOGGER.severe("device id not found");
	    return null;

	}

	if (topic.getType() == MessageType.DBIRTH || topic.getType() == MessageType.DDATA) {

	    SPHADeviceRemote device = (SPHADeviceRemote) getDevice(topic.getDeviceId());

	    // if not found, create
	    if (device == null) {

		device = new SPHADeviceRemote(topic, this);
		addDevice(device);

	    }

	    device.setPayload(payload);
	    return device;

	}

	return null;

    }

}
