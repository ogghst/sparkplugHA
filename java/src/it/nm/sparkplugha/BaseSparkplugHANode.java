package it.nm.sparkplugha;

import static org.eclipse.tahu.message.model.MetricDataType.Boolean;
import static org.eclipse.tahu.message.model.MetricDataType.Int64;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;

public class BaseSparkplugHANode {

	protected String hwVersion = "Emulated Hardware";
	protected String swVersion = "v1.0.0";
	protected static final String NAMESPACE = "spBv1.0";

	protected String groupId = "Sparkplug B Home Automation Devices";
	protected String edgeNode = "NDEDGENODE";
	protected String deviceId = "NDDEVICEID";
	protected String clientId = "NDCLIENTID";
	private int bdSeq = 0;
	private int seq = 0;

	private Hashtable<String, BaseSpHAMetric> metrics;

	private SparkplugBPayload nodeBirthPayload;

	private final static Logger LOGGER = Logger.getLogger(BaseSparkplugHANode.class.getName());

	public BaseSparkplugHANode() {
		super();
		metrics = new Hashtable<String, BaseSpHAMetric>();
	}

	public SparkplugBPayloadBuilder addBdSeqNum(SparkplugBPayloadBuilder payload) throws Exception {
		if (payload == null) {
			payload = new SparkplugBPayloadBuilder();
		}
		if (bdSeq == 256) {
			bdSeq = 0;
		}
		payload.addMetric(new MetricBuilder("bdSeq", Int64, (long) bdSeq).createMetric());
		bdSeq++;
		return payload;
	}

	protected SparkplugBPayloadBuilder createNodeDeathPayload() throws Exception {
		// Build up DEATH payload - note DEATH payloads don't have a regular sequence
		// number
		SparkplugBPayloadBuilder deathPayload = new SparkplugBPayloadBuilder().setTimestamp(new Date());
		deathPayload = addBdSeqNum(deathPayload);

		return deathPayload;

	}

	protected SparkplugBPayload createNodeBirthPayload() throws Exception {

		// Create the BIRTH payload and set the position and other metrics
		SparkplugBPayload payload = new SparkplugBPayload(new Date(), new ArrayList<Metric>(), getSeqNum(), newUUID(),
				null);

		payload.addMetric(new MetricBuilder("bdSeq", Int64, (long) bdSeq).createMetric());
		payload.addMetric(new MetricBuilder("Node Control/Rebirth", Boolean, false).createMetric());

		for (BaseSpHAMetric metric : metrics.values()) {
			payload.addMetric(new MetricBuilder(metric.getName(), metric.getType(), metric.getValue()).createMetric());
		}

		return payload;

	}

	protected SparkplugBPayload createDataPayload() throws Exception {

		// Create the BIRTH payload and set the position and other metrics
		SparkplugBPayload payload = new SparkplugBPayload(new Date(), new ArrayList<Metric>(), getSeqNum(), newUUID(),
				null);
		return payload;

	}

	protected SparkplugBPayload createSpHaMetricPayload(String name) throws Exception {

		SparkplugBPayload outboundPayload = createDataPayload();
		BaseSpHAMetric spHAMetric = getSpHAMetricByName(name);
		if (spHAMetric == null) {
			throw new SpHAMetricNotFoundException("No Metric with name '" + name + "', ignoring");
		}

		outboundPayload.addMetric(
				new MetricBuilder(spHAMetric.getName(), spHAMetric.getType(), spHAMetric.getValue()).createMetric());
		return outboundPayload;
	}

	public void addSpHAMetric(BaseSpHAMetric aMetric) {
		metrics.put(aMetric.getName(), aMetric);
	}

	public BaseSpHAMetric getSpHAMetricByName(String name) {
		return metrics.get(name);
	}

	public void updateSpHAMetricValue(String name, Object value) throws SpHAMetricNotFoundException {
		BaseSpHAMetric spHAMetric = getSpHAMetricByName(name);
		if (spHAMetric == null) {
			throw new SpHAMetricNotFoundException("No Metric with name '" + name + "', ignoring");
		}

		spHAMetric.setValue(value);
		metrics.replace(name, spHAMetric);

	}

	public long getSeqNum() throws Exception {
		if (seq == 256) {
			seq = 0;
		}
		return seq++;
	}

	public long resetSeqNum() {
		seq = 0;
		return seq;
	}

	protected String newUUID() {
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

	public String getEdgeNode() {
		return edgeNode;
	}

	public void setEdgeNode(String edgeNode) {
		this.edgeNode = edgeNode;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
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

}