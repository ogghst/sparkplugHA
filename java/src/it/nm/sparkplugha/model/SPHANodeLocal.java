package it.nm.sparkplugha.model;

import static org.eclipse.tahu.message.model.MetricDataType.Int64;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.message.model.Topic;

public abstract class SPHANodeLocal extends SPHANode {

    protected String clientId = "NDCLIENTID";

    private Object seqLock = new Object();

    private SparkplugBPayload nodeBirthPayload;

    // TODO remove
    private SparkplugBPayload payload;

    private final static Logger LOGGER = Logger.getLogger(SPHANodeLocal.class.getName());

    public SPHANodeLocal(String groupId, String edgeNodeId, SPHANodeState state) throws Exception {

	super(groupId, edgeNodeId, state);
	this.payload = createNodeBirthPayload();
    }

    public SparkplugBPayload createNodeDeathPayload() throws Exception {

	// Build up DEATH payload - note DEATH payloads don't have a regular sequence
	// number
	SparkplugBPayloadBuilder deathPayload = new SparkplugBPayloadBuilder().setTimestamp(new Date());

	if (bdSeq == 256) {

	    bdSeq = 0;

	}

	deathPayload.addMetric(new MetricBuilder(BD_SEQ, Int64, (long) bdSeq).createMetric());
	bdSeq++;

	return deathPayload.createPayload();

    }

    private SparkplugBPayload createNodeBirthPayload() throws Exception {

	synchronized (seqLock) {

	    // Reset the sequence number
	    seq = 0;

	    // Create the BIRTH payload and set the position and other metrics
	    SparkplugBPayload payload = createPayload();

	    payload.addMetric(new MetricBuilder(BD_SEQ, Int64, (long) bdSeq).createMetric());
	    payload.addMetric(new MetricBuilder(NODE_CONTROL_REBIRTH, MetricDataType.Boolean, false).createMetric());
	    payload.addMetric(new MetricBuilder(NODE_CONTROL_REBOOT, MetricDataType.Boolean, false).createMetric());
	    payload.addMetric(
		    new MetricBuilder(NODE_CONTROL_NEXT_SERVER, MetricDataType.Boolean, false).createMetric());
	    payload.addMetric(new MetricBuilder(NODE_CONTROL_SCAN_RATE, MetricDataType.Int64, 1000l).createMetric());

	    /*
	    for (Metric metric : getMetrics()) {

		payload.addMetric(
			new MetricBuilder(metric.getName(), metric.getDataType(), metric.getValue()).createMetric());

	    }
	    */

	    //TODO features are on devices, CHANGE
	    /*
	    for (SPHAFeature feature : getFeatures()) {

		payload.addMetric(
			new MetricBuilder(feature.getName(), MetricDataType.Template, feature.getTemplateDefinition())
				.createMetric());

	    }
	    */

	    return payload;

	}

    }

    public SparkplugBPayload createPayload() {

	SparkplugBPayload p = new SparkplugBPayload(new Date(), new ArrayList<Metric>(), getSeq(), newUUID(),
		null);
	return p;

    }

    /*
    public SparkplugBPayload createSPHAMetricPayload(SPHAMetric spHAMetric) throws Exception {

	SparkplugBPayload outboundPayload = createPayload();

	if (spHAMetric == null) {

	    throw new SpHAMetricNotFoundException("No Metric, ignoring");

	}

	outboundPayload
		.addMetric(new MetricBuilder(spHAMetric.getName(), spHAMetric.getDataType(), spHAMetric.getValue())
			.createMetric());
	return outboundPayload;

    }
    */
    

    public Metric createMetric(String name, MetricDataType type, Object value) throws SparkplugInvalidTypeException {

	Metric metric = new MetricBuilder(name, type, value).createMetric();
	payload.addMetric(metric);
	return metric;

    }

    public String getClientId() {

	return clientId;

    }

    public void setClientId(String clientId) {

	this.clientId = clientId;

    }


    public SparkplugBPayload getPayload() {

	return payload;

    }

    public void setPayload(Topic topic, SparkplugBPayload payload) {

	this.payload = payload;

    }

    public void setNodeBirthPayload(SparkplugBPayload nodeBirthPayload) {

	this.nodeBirthPayload = nodeBirthPayload;

    }

}