package it.nm.sparkplugha.model;

import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Topic;

public class SPHANodeRemote extends SPHANode {
    
    //TODO remove
    private SparkplugBPayload payload;

    public SPHANodeRemote(Topic topic, SPHANodeState state, SparkplugBPayload payload) {

	super(topic.getGroupId(), topic.getEdgeNodeId(), state, payload);
	//buildFeatures(topic, payload);
	//buildMetrics(topic, payload);
	

    }

    private void buildFeatures(Topic topic, SparkplugBPayload payload) {

	if (topic == null)
	    return;

	if (topic.getType() == MessageType.DBIRTH || topic.getType() == MessageType.DDATA) {

	    for (Metric metric : payload.getMetrics()) {

		String deviceId = topic.getDeviceId();

		// search for metric
		if (deviceId != null) {

		    for (SPHAFeature feature : getFeatures()) {

			if (feature.getName().equals(metric.getName())) {
			    // feature found

			}

		    }

		}

	    }

	}

    }

}
