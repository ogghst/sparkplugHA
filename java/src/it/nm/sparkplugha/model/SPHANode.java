package it.nm.sparkplugha.model;

import static org.eclipse.tahu.message.model.MetricDataType.Int64;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;

import it.nm.sparkplugha.exceptions.SpHAMetricNotFoundException;

public abstract class SPHANode {

    protected String hwVersion = "Emulated Hardware";
    protected String swVersion = "v1.0.0";
    protected static final String NAMESPACE = "spBv1.0";
    protected String groupId = "SparkplugHA";
    protected String edgeNodeId = "NDEDGENODE";
    protected String clientId = "NDCLIENTID";

    private int bdSeq = 0;
    private int seq = 0;

    private Object seqLock = new Object();

    protected Hashtable<String, SPHAMetric> metrics;
    protected Hashtable<String, SPHAFeature> features;

    private SparkplugBPayload nodeBirthPayload;

    private final static Logger LOGGER = Logger.getLogger(SPHANode.class.getName());

    public SPHANode() {

	super();
	metrics = new Hashtable<String, SPHAMetric>();
	features = new Hashtable<String, SPHAFeature>();

    }

    protected void addFeature(SPHAFeature feature) {

	features.put(feature.getName(), feature);

    }

    protected Collection<SPHAFeature> getFeatures() {

	return features.values();

    }

    public SparkplugBPayload createNodeDeathPayload() throws Exception {

	// Build up DEATH payload - note DEATH payloads don't have a regular sequence
	// number
	SparkplugBPayloadBuilder deathPayload = new SparkplugBPayloadBuilder().setTimestamp(new Date());

	if (bdSeq == 256) {

	    bdSeq = 0;

	}

	deathPayload.addMetric(new MetricBuilder("bdSeq", Int64, (long) bdSeq).createMetric());
	bdSeq++;

	return deathPayload.createPayload();

    }

    public SparkplugBPayload createNodeBirthPayload() throws Exception {

	synchronized (seqLock) {

	    // Reset the sequence number
	    seq = 0;

	    // Create the BIRTH payload and set the position and other metrics
	    SparkplugBPayload payload = createPayload();

	    payload.addMetric(new MetricBuilder("bdSeq", Int64, (long) bdSeq).createMetric());
	    payload.addMetric(new MetricBuilder("Node Control/Rebirth", MetricDataType.Boolean, false).createMetric());
	    payload.addMetric(new MetricBuilder("Node Control/Reboot", MetricDataType.Boolean, false).createMetric());
	    payload.addMetric(
		    new MetricBuilder("Node Control/Next Server", MetricDataType.Boolean, false).createMetric());
	    payload.addMetric(new MetricBuilder("Node Control/Scan Rate", MetricDataType.Int64, 1000l).createMetric());

	    for (SPHAMetric metric : metrics.values()) {

		payload.addMetric(
			new MetricBuilder(metric.getName(), metric.getDataType(), metric.getValue()).createMetric());

	    }

	    for (SPHAFeature feature : features.values()) {

		payload.addMetric(
			new MetricBuilder(feature.getName(), MetricDataType.Template, feature.getTemplateDefinition())
				.createMetric());

	    }

	    return payload;

	}

    }

    public SparkplugBPayload createPayload() {

	SparkplugBPayload payload = new SparkplugBPayload(new Date(), new ArrayList<Metric>(), getSeqNum(), newUUID(),
		null);
	return payload;

    }

    public SparkplugBPayload createSPHAMetricPayload(SPHAMetric spHAMetric) throws Exception {

	SparkplugBPayload outboundPayload = createPayload();

	if (spHAMetric == null) {

	    throw new SpHAMetricNotFoundException("No Metric, ignoring");

	}

	outboundPayload.addMetric(
		new MetricBuilder(spHAMetric.getName(), spHAMetric.getDataType(), spHAMetric.getValue()).createMetric());
	return outboundPayload;

    }

    public SPHAMetric createSPHAMetric(String name, MetricDataType dataType, Object initialValue) throws SparkplugInvalidTypeException {

	SPHAMetric aMetric = new SPHAMetric(name, dataType, initialValue);
	metrics.put(aMetric.getName(), aMetric);
	return aMetric;

    }

    public SPHAMetric getSPHAMetricByName(String name) {

	return metrics.get(name);

    }

    /*
     * public BaseSpHAMetric updateSpHAMetricValue(String name, Object value) throws
     * SpHAMetricNotFoundException { BaseSpHAMetric spHAMetric =
     * getSpHAMetricByName(name); if (spHAMetric == null) { throw new
     * SpHAMetricNotFoundException("No Metric with name '" + name + "', ignoring");
     * } return updateSpHAMetric(spHAMetric); }
     */

    public SPHAMetric updateSPHAMetric(SPHAMetric metric) throws Exception {

	if (metrics.replace(metric.getName(), metric) == null) {

	    throw new SpHAMetricNotFoundException("No Metric with name '" + metric.getName() + "', ignoring");

	}

	publishNodeData(createSPHAMetricPayload(metric));

	return metric;

    }

    public long getSeqNum() {

	if (seq == 256) {

	    seq = 0;

	}

	return seq++;

    }

    public long resetSeqNum() {

	seq = 0;
	return seq;

    }

    private String newUUID() {

	return java.util.UUID.randomUUID().toString();

    }

    public String getHwVersion() {

	return hwVersion;

    }

    public void setHwVersion(String hwVersion) {

	this.hwVersion = hwVersion;

    }

    public String getSwVersion() {

	return swVersion;

    }

    public void setSwVersion(String swVersion) {

	this.swVersion = swVersion;

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

    public String getClientId() {

	return clientId;

    }

    public void setClientId(String clientId) {

	this.clientId = clientId;

    }

    public SparkplugBPayload getNodeBirthPayload() {

	return nodeBirthPayload;

    }

    public void setNodeBirthPayload(SparkplugBPayload nodeBirthPayload) {

	this.nodeBirthPayload = nodeBirthPayload;

    }

    public abstract void publishNodeData(SparkplugBPayload payload) throws Exception;

    public abstract void publishNodeCommand(SPHAEdgeNodeDescriptor descriptor, SparkplugBPayload payload)
	    throws Exception;

    public abstract void publishFeatureData(SPHAFeature feature, SparkplugBPayload payload) throws Exception;

    public abstract void publishFeatureCommand(SPHAFeature feature, SPHAEdgeNodeDescriptor descriptor,
	    SparkplugBPayload payload) throws Exception;

}