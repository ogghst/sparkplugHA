package it.nm.sparkplugha;

import static org.eclipse.tahu.message.model.MetricDataType.Int64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.util.CompressionAlgorithm;
import org.eclipse.tahu.util.PayloadUtil;

import it.nm.sparkplugha.SPHANode.SPHANodeState;
import it.nm.sparkplugha.exceptions.SpHAMetricNotFoundException;
import it.nm.sparkplugha.model.SPHAFeature;
import it.nm.sparkplugha.model.SPHAMetric;
import it.nm.sparkplugha.mqtt.MQTTClientInstance;
import it.nm.sparkplugha.mqtt.MQTTPublisher;
import it.nm.sparkplugha.mqtt.MQTTSPHANode;

public class SPHANodeManager implements MqttCallback {

    private final static Logger LOGGER = Logger.getLogger(SPHANodeManager.class.getName());
    public static final String NAMESPACE = "spBv1.0";

    private boolean USING_COMPRESSION = false;

    private SPHANode node;
    MQTTClientInstance client;

    private Object seqLock = new Object();

    public SPHANodeManager(SPHANode node, String url, String clientId) {

	this.node = node;
	client = MQTTClientInstance.getInstance();

	client.config(url, clientId,
		NAMESPACE + "/" + node.getGroupId() + "/" + MessageType.NDEATH + "/" + node.getEdgeNodeId(),
		payloadToBytes(createNodeDeathPayload()));

	client.addCallback(this);

    }

    public void connect() throws Exception {

	// Subscribe to control/command messages for both the edge of network node and
	// the attached devices
	client.subscribe(
		NAMESPACE + "/" + node.getGroupId() + "/" + MessageType.NCMD + "/" + node.getEdgeNodeId() + "/#", 0);
	client.subscribe(
		NAMESPACE + "/" + node.getGroupId() + "/" + MessageType.DCMD + "/" + node.getEdgeNodeId() + "/#", 0);

	client.subscribe(Utils.SCADA_NAMESPACE + "/#", 0);

	for (SPHAFeature feature : node.getFeatures()) {

	    subscribeFeature(feature);

	}

    }

    public void subscribeFeature(SPHAFeature feature) throws MqttException {

	LOGGER.fine("Subscribing feature: " + feature.getName() + ". Topic = '" + feature.getTopic() + "'");

	String[] dataTopics = feature.getListeningDeviceDataTopics();
	// String[] commandTopics = feature.getListeningDeviceCommandTopics();

	for (int i = 0; i < dataTopics.length; i++) {

	    LOGGER.fine("	Listening Device Data Topic = '" + dataTopics[i] + "'");

	    client.subscribe(NAMESPACE + "/" + node.getGroupId() + "/" + MessageType.DDATA + "/+/" + dataTopics[i], 0);

	}

    }

    public void unsubscribeFeature(SPHAFeature feature) throws MqttException {

	LOGGER.fine("Unsbscribing feature: " + feature.getName() + ". Topic = '" + feature.getTopic() + "'");

	String[] dataTopics = feature.getListeningDeviceDataTopics();
	// String[] commandTopics = feature.getListeningDeviceCommandTopics();

	for (int i = 0; i < dataTopics.length; i++) {

	    LOGGER.fine("	Listening Device Data Topic = '" + dataTopics[i] + "'");

	    client.unsubscribe(NAMESPACE + "/" + node.getGroupId() + "/" + MessageType.DDATA + "/+/" + dataTopics[i]);

	}

    }

    protected void disconnect() throws Exception {

	client.unsubscribe(
		NAMESPACE + "/" + node.getGroupId() + "/" + MessageType.NCMD + "/" + node.getEdgeNodeId() + "/#");
	client.unsubscribe(
		NAMESPACE + "/" + node.getGroupId() + "/" + MessageType.DCMD + "/" + node.getEdgeNodeId() + "/#");

	client.unsubscribe(Utils.SCADA_NAMESPACE + "/#");

	for (SPHAFeature feature : node.getFeatures()) {

	    unsubscribeFeature(feature);

	}

	new MQTTPublisher(client.getClient(),
		NAMESPACE + "/" + node.getGroupId() + "/" + MessageType.NDEATH + "/" + node.getEdgeNodeId(),
		createNodeDeathPayload(), 1, true, USING_COMPRESSION).publish();

	client.disconnect();

    }

    public SparkplugBPayload createNodeDeathPayload() throws Exception {

	// Build up DEATH payload - note DEATH payloads don't have a regular sequence
	// number
	SparkplugBPayloadBuilder deathPayload = new SparkplugBPayloadBuilder().setTimestamp(new Date());

	if (node.getBdSeq() == 256) {

	    node.setBdSeq(0);

	}

	deathPayload.addMetric(new MetricBuilder(SPHANode.BD_SEQ, Int64, node.getBdSeq()).createMetric());

	node.setBdSeq(node.getBdSeq() + 1);

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

    public SparkplugBPayload createPayload() {

	SparkplugBPayload payload = new SparkplugBPayload(new Date(), new ArrayList<Metric>(), getSeq(), newUUID(),
		null);
	return payload;

    }

    private byte[] payloadToBytes(SparkplugBPayload payload) throws IOException, SparkplugException {

	byte[] bytes;

	if (USING_COMPRESSION) {

	    // Compress payload (optional)
	    bytes = new SparkplugBPayloadEncoder().getBytes(PayloadUtil.compress(payload, compressionAlgorithm));

	} else {

	    bytes = new SparkplugBPayloadEncoder().getBytes(payload);

	}

	return bytes;

    }

    public void setPayload(SparkplugBPayload payload) {

	this.payload = payload;
	buildFeatures(payload);
	buildMetrics(payload);

    }

    private void buildFeatures(SparkplugBPayload payload) {

	// TODO Auto-generated method stub

    }

    private void buildMetrics(SparkplugBPayload payload) {

	// TODO Auto-generated method stub

    }

    @Override
    public void connectionLost(Throwable cause) {

	// TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

	// TODO Auto-generated method stub

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

	// TODO Auto-generated method stub

    }

    public void publishMetricData(String metric) throws Exception {

	// TODO Auto-generated method stub

    }

    public void publishMetricCommand(EdgeNodeDescriptor descriptor, SPHAMetric metric) throws Exception {

	// TODO Auto-generated method stub

    }

    public void publishFeatureData(String feature) throws Exception {

	// TODO Auto-generated method stub

    }

    @Override
    public void publishFeatureCommand(EdgeNodeDescriptor descriptor, String feature) throws Exception {

	// TODO Auto-generated method stub

    }

}
