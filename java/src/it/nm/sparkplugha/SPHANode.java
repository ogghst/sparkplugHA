package it.nm.sparkplugha;

import static org.eclipse.tahu.message.model.MetricDataType.Int64;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;

import com.fasterxml.jackson.annotation.JsonValue;

import it.nm.sparkplugha.exceptions.SpHAMetricNotFoundException;
import it.nm.sparkplugha.model.SPHAFeature;
import it.nm.sparkplugha.model.SPHAMetric;

public abstract class SPHANode {

    protected static final String NAMESPACE = "spBv1.0";
    private static final String NODE_CONTROL_SCAN_RATE = "Node Control/Scan Rate";
    private static final String NODE_CONTROL_NEXT_SERVER = "Node Control/Next Server";
    private static final String NODE_CONTROL_REBOOT = "Node Control/Reboot";
    private static final String NODE_CONTROL_REBIRTH = "Node Control/Rebirth";
    private static final String BD_SEQ = "bdSeq";

    protected String hwVersion = "Emulated Hardware";
    protected String swVersion = "v1.0.0";

    protected String clientId = "NDCLIENTID";

    private int bdSeq = 0;
    private long seq = 0;

    private Object seqLock = new Object();

    protected Hashtable<String, SPHAMetric> metrics;
    protected Hashtable<String, SPHAFeature> features;

    private SparkplugBPayload nodeBirthPayload;

    private SPHANodeState state;
    private SparkplugBPayload payload;

    public enum SPHANodeState {
	OFFLINE, ONLINE
    }

    private final String groupId;
    private final String edgeNodeId;

    public String getGroupId() {

	return groupId;

    }

    public String getEdgeNodeId() {

	return edgeNodeId;

    }

    public String getDescriptorString() {

	return groupId + "/" + edgeNodeId;

    }

    @Override
    public int hashCode() {

	return this.getDescriptorString().hashCode();

    }

    @Override
    public boolean equals(Object object) {

	if (object instanceof EdgeNodeDescriptor) {

	    return this.getDescriptorString().equals(((EdgeNodeDescriptor) object).getDescriptorString());

	}

	return this.getDescriptorString().equals(object);

    }

    @Override
    @JsonValue
    public String toString() {

	return getDescriptorString();

    }

    private final static Logger LOGGER = Logger.getLogger(SPHANode.class.getName());

    public SPHANode(String groupId, String edgeNodeId, SPHANodeState state, SparkplugBPayload payload) {

	metrics = new Hashtable<String, SPHAMetric>();
	features = new Hashtable<String, SPHAFeature>();
	this.groupId = groupId;
	this.edgeNodeId = edgeNodeId;
	this.state = state;
	this.seq = 0;
	this.payload = payload;

	buildMetrics(payload);
	buildFeatures(payload);

    }

    private void buildFeatures(SparkplugBPayload payload) {

	// TODO Auto-generated method stub

    }

    private void buildMetrics(SparkplugBPayload payload) {

	// TODO Auto-generated method stub

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

	deathPayload.addMetric(new MetricBuilder(BD_SEQ, Int64, (long) bdSeq).createMetric());
	bdSeq++;

	return deathPayload.createPayload();

    }

    public SparkplugBPayload createNodeBirthPayload() throws Exception {

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

	SparkplugBPayload payload = new SparkplugBPayload(new Date(), new ArrayList<Metric>(), getSeq(),
		newUUID(), null);
	return payload;

    }

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

    public SPHAMetric createSPHAMetric(String name, MetricDataType dataType, Object initialValue)
	    throws SparkplugInvalidTypeException {

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

	return metric;

    }

    public long increaseSeq() {

	if (seq == 256) {

	    seq = 0;

	}

	return ++seq;

    }

    public long getSeq() {

	return seq;

    }

    public void setSeq(long seq) {

	this.seq = seq;

    }

    public long resetSeq() {

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

    public String getClientId() {

	return clientId;

    }

    public void setClientId(String clientId) {

	this.clientId = clientId;

    }

    public SparkplugBPayload getNodeBirthPayload() {

	return nodeBirthPayload;

    }

    public void setState(SPHANodeState state) {

	LOGGER.fine("State: " + state);
	this.state = state;

    }

    public SPHANodeState getState() {

	return state;

    }

    public SparkplugBPayload getPayload() {

	return payload;

    }

    public void setPayload(SparkplugBPayload payload) {

	this.payload = payload;
	buildFeatures(payload);
	buildMetrics(payload);

    }

    public void setNodeBirthPayload(SparkplugBPayload nodeBirthPayload) {

	this.nodeBirthPayload = nodeBirthPayload;

    }

}