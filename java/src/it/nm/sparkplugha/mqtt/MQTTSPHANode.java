package it.nm.sparkplugha.mqtt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.util.CompressionAlgorithm;
import org.eclipse.tahu.util.PayloadUtil;

import it.nm.sparkplugha.SPHAUtils;
import it.nm.sparkplugha.model.SPHADevice;
import it.nm.sparkplugha.model.SPHADeviceLocal;
import it.nm.sparkplugha.model.SPHANodeLocal;

public abstract class MQTTSPHANode extends SPHANodeLocal implements MqttCallbackExtended {

    public MQTTSPHANode(String groupId, String edgeNodeId, SPHANodeState state) throws Exception {

	super(groupId, edgeNodeId, state);

    }

    private final static Logger LOGGER = Logger.getLogger(MQTTSPHANode.class.getName());

    protected MqttClient client;
    private CompressionAlgorithm compressionAlgorithm = CompressionAlgorithm.GZIP;
    private ExecutorService executor;
    private Object seqLock = new Object();
    private String serverPassword = "";
    private String serverUrl = "tcp://localhost:1883";
    private String serverUsername = "";
    private boolean USING_COMPRESSION = false;
    private boolean USING_REAL_TLS = false;

    public abstract void reboot();

    public abstract void nextserver();

    public abstract void setScanRate(int scanRate);

    protected void connect() throws Exception {

	// Random generator and thread pool for outgoing published messages
	executor = Executors.newFixedThreadPool(1);

	MqttConnectOptions options = new MqttConnectOptions();

	if (USING_REAL_TLS) {

	    SocketFactory sf = SSLSocketFactory.getDefault();
	    options.setSocketFactory(sf);

	}

	// Connect to the MQTT Server
	options.setAutomaticReconnect(true);
	options.setCleanSession(true);
	options.setConnectionTimeout(30);
	options.setKeepAliveInterval(30);
	options.setUserName(serverUsername);
	options.setPassword(serverPassword.toCharArray());

	options.setWill(NAMESPACE + "/" + getGroupId() + "/" + MessageType.NDEATH + "/" + getEdgeNodeId(),
		payloadToBytes(createNodeDeathPayload()), 0, false);

	client = new MqttClient(serverUrl, clientId);
	client.setTimeToWait(2000);
	client.setCallback(this); // short timeout on failure to connect
	client.connect(options);

	LOGGER.info("connected to '" + serverUrl + "' with ClientID '" + clientId + "'");

	// Subscribe to control/command messages for both the edge of network node and
	// the attached devices
	client.subscribe(NAMESPACE + "/" + getGroupId() + "/" + MessageType.NCMD + "/" + getEdgeNodeId() + "/#", 0);
	client.subscribe(NAMESPACE + "/" + getGroupId() + "/" + MessageType.DCMD + "/" + getEdgeNodeId() + "/#", 0);

	// TODO manage SCADA state message
	client.subscribe(SPHAUtils.SCADA_NAMESPACE + "/#", 0);

	for (SPHADevice device : getDevices()) {

	    subscribeFeature(device);

	}

    }

    private void subscribeFeature(SPHADevice feature) throws MqttException {

	LOGGER.fine("Subscribing feature: " + feature.getDeviceId() + ". Topic = '" + feature.getDeviceId() + "'");

	// client.subscribe(NAMESPACE + "/" + groupId + "/" + MessageType.DCMD + "/" +
	// edgeNode + "/" + feature.getTopic(),
	// 0);

	String[] dataTopics = feature.getListeningDeviceDataTopics();
	// String[] commandTopics = feature.getListeningDeviceCommandTopics();

	for (int i = 0; i < dataTopics.length; i++) {

	    LOGGER.fine("	Listening Device Data Topic = '" + dataTopics[i] + "'");

	    client.subscribe(NAMESPACE + "/" + getGroupId() + "/" + MessageType.DDATA + "/+/" + dataTopics[i], 0);

	}

	/*
	 * for (int i = 0; i < commandTopics.length; i++) {
	 * 
	 * LOGGER.fine("	Listening Device Command Topic = '" + dataTopics[i] +
	 * "'");
	 * 
	 * client.subscribe(NAMESPACE + "/" + groupId + "/" + MessageType.DCMD + "/+/" +
	 * commandTopics[i], 0);
	 * 
	 * }
	 */

    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

	LOGGER.fine("Connection estabilished with '" + serverURI + "', reconnect = " + reconnect);

	try {

	    publishNodeBirth();

	} catch (SparkplugInvalidTypeException e) {

	    LOGGER.log(Level.SEVERE, e.getMessage(), e);

	}

    }

    @Override
    public void connectionLost(Throwable cause) {

	LOGGER.log(Level.WARNING, "Connection lost. Cause: " + cause.toString(), cause);

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

	LOGGER.fine("Published message: " + Arrays.toString(token.getTopics()));

	try {

	    LOGGER.finest(new String(token.getMessage().getPayload(), StandardCharsets.UTF_8));

	} catch (Exception e) {

	}

    }

    protected void disconnect() throws Exception {

	client.unsubscribe(NAMESPACE + "/" + getGroupId() + "/" + MessageType.NCMD + "/" + getEdgeNodeId() + "/#");
	// client.unsubscribe(NAMESPACE + "/" + groupId + "/NDATA/" + edgeNode + "/#");
	// client.unsubscribe(NAMESPACE + "/" + groupId + "/DCMD/" + edgeNode + "/#");
	// client.unsubscribe(NAMESPACE + "/#");

	new MQTTPublisher(client, NAMESPACE + "/" + getGroupId() + "/" + MessageType.NDEATH + "/" + getEdgeNodeId(),
		createNodeDeathPayload(), 1, true, USING_COMPRESSION).publish();

	client.disconnect();

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

    public String getServerPassword() {

	return serverPassword;

    }

    public String getServerUrl() {

	return serverUrl;

    }

    public String getServerUsername() {

	return serverUsername;

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

	LOGGER.fine("Message Arrived on topic " + topic);

	String[] splitTopic = topic.split("/");

	if (splitTopic[0].equals(SPHAUtils.SCADA_NAMESPACE)) {

	    LOGGER.fine("SCADA update: " + splitTopic[1]);
	    return;

	}

	SparkplugBPayloadDecoder decoder = new SparkplugBPayloadDecoder();
	SparkplugBPayload inboundPayload = decoder.buildFromByteArray(message.getPayload());

	// Debug
	for (Metric metric : inboundPayload.getMetrics()) {

	    LOGGER.fine("	Metric " + metric.getName() + "=" + metric.getValue());

	}

	if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(getGroupId()) && splitTopic[2].equals("NCMD")
		&& splitTopic[3].equals(getEdgeNodeId())) {

	    for (Metric metric : inboundPayload.getMetrics()) {

		if ("Node Control/Rebirth".equals(metric.getName()) && ((Boolean) metric.getValue())) {

		    publishNodeBirth();

		} else if ("Node Control/Reboot".equals(metric.getName()) && ((Boolean) metric.getValue())) {

		    reboot();

		} else if ("Node Control/Next Server".equals(metric.getName()) && ((Boolean) metric.getValue())) {

		    nextserver();

		} else if ("Node Control/Scan Rate".equals(metric.getName())) {

		    setScanRate(((Integer) metric.getValue()).intValue());

		} else {

		    LOGGER.warning("Unknown Node Command NCMD: " + metric.getName());

		}

	    }

	}

	/*
	 * else if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) &&
	 * splitTopic[2].equals("DCMD") && splitTopic[3].equals(edgeNode)) {
	 * 
	 * LOGGER.fine("Command received for device " + splitTopic[4]);
	 * 
	 * for (Metric metric : inboundPayload.getMetrics()) {
	 * 
	 * String name = metric.getName(); Object value = metric.getValue();
	 * 
	 * SPHAMetric spHAMetric = getSpHAMetricByName(name);
	 * 
	 * if (spHAMetric == null) {
	 * 
	 * LOGGER.warning("No Metric with name '" + name + "', ignoring");
	 * 
	 * } else {
	 * 
	 * updateSpHAMetric(spHAMetric); publishNodeData(name);
	 * 
	 * }
	 * 
	 * }
	 * 
	 * } else if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId)
	 * && splitTopic[2].equals("DDATA") && splitTopic[3].equals(edgeNode)) {
	 * 
	 * for (Metric metric : inboundPayload.getMetrics()) {
	 * 
	 * // forward to features for (SPHAFeature feature : features.values()) {
	 * 
	 * feature.DataArrived(metric);
	 * 
	 * }
	 * 
	 * }
	 * 
	 * }
	 */

	for (SPHADevice _f : getDevices()) {

	    SPHADeviceLocal feature = (SPHADeviceLocal) _f;

	    Vector<String> dT = new Vector<String>(Arrays.asList(feature.getListeningDeviceDataTopics()));
	    // Vector<String> cT = new
	    // Vector<String>(Arrays.asList(feature.getListeningDeviceCommandTopics()));

	    // addressed directly to the feature
	    if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(getGroupId()) && splitTopic[2].equals("DCMD")
		    && splitTopic[3].equals(getEdgeNodeId()) && splitTopic[4].equals(feature.getDeviceId())) {

		for (Metric metric : inboundPayload.getMetrics()) {

		    feature.CommandArrived(new EdgeNodeDescriptor(splitTopic[1], splitTopic[3]), metric);

		}

	    }

	    // message to a listening topic
	    else if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(getGroupId())
		    && splitTopic[2].equals("DDATA") && !splitTopic[3].equals(getEdgeNodeId())
		    && dT.contains(splitTopic[4])) {

		for (Metric metric : inboundPayload.getMetrics()) {

		    feature.DataArrived(new EdgeNodeDescriptor(splitTopic[1], splitTopic[3]), metric);

		}

	    }
	    /*
	     * else if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) &&
	     * splitTopic[2].equals("DCMD") && cT.contains(splitTopic[4])) {
	     * 
	     * for (Metric metric : inboundPayload.getMetrics()) {
	     * 
	     * feature.CommandArrived(new SPHAEdgeNodeDescriptor(splitTopic[1],
	     * splitTopic[3]), metric);
	     * 
	     * }
	     * 
	     * }
	     */

	}

    }

    protected void publishNodeBirth() throws SparkplugInvalidTypeException {

	synchronized (seqLock) {

	    // Reset the sequence number
	    resetSeq();
	    executor.execute(new MQTTPublisher(client, NAMESPACE + "/" + getGroupId() + "/NBIRTH/" + getEdgeNodeId(),
		    getPayload(), 0, false, USING_COMPRESSION));

	    for (SPHADevice feature : getDevices()) {

		feature.getPayload().setSeq(increaseSeq());

		executor.execute(new MQTTPublisher(client,
			NAMESPACE + "/" + getGroupId() + "/DBIRTH/" + getEdgeNodeId() + "/" + feature.getDeviceId(),
			feature.getPayload(), 0, false, USING_COMPRESSION));

	    }

	}

    }

    /*
     * public void publishNodeData(String metricName) throws Exception {
     * 
     * publishNodeData(createSpHaMetricPayload(metricName));
     * 
     * }
     */

    public void publishNodeData(SparkplugBPayload payload)
	    throws MqttPersistenceException, MqttException, IOException, SparkplugException {

	if (client.isConnected()) {

	    synchronized (seqLock) {

		payload.setSeq(increaseSeq());
		executor.execute(new MQTTPublisher(client, NAMESPACE + "/" + getGroupId() + "/NDATA/" + getEdgeNodeId(),
			payload, 0, false, USING_COMPRESSION));

	    }

	} else {

	    // TODO store and forward
	    throw new IOException("Not connected - not publishing data");

	}

    }

    public void publishNodeCommand(SPHANodeLocal descriptor, SparkplugBPayload payload) throws Exception {

	if (client.isConnected()) {

	    synchronized (seqLock) {

		payload.setSeq(increaseSeq());
		executor.execute(new MQTTPublisher(client,
			NAMESPACE + "/" + descriptor.getGroupId() + "/NCMD/" + descriptor.getEdgeNodeId(), payload, 0,
			false, USING_COMPRESSION));

	    }

	} else {

	    // TODO store and forward
	    throw new IOException("Not connected - not publishing data");

	}

    }

    public void publishFeatureData(SPHADevice feature, SparkplugBPayload payload) throws Exception {

	if (client.isConnected()) {

	    synchronized (seqLock) {

		payload.setSeq(increaseSeq());
		executor.execute(new MQTTPublisher(client,
			NAMESPACE + "/" + getGroupId() + "/DDATA/" + getEdgeNodeId() + "/" + feature.getDeviceId(),
			payload, 0, false, USING_COMPRESSION));

	    }

	} else {

	    // TODO store and forward
	    throw new IOException("Not connected - not publishing data");

	}

    }

    public void publishFeatureCommand(SPHADevice feature, EdgeNodeDescriptor descriptor, SparkplugBPayload payload)
	    throws Exception {

	if (client.isConnected()) {

	    synchronized (seqLock) {

		payload.setSeq(increaseSeq());
		executor.execute(new MQTTPublisher(client, NAMESPACE + "/" + descriptor.getGroupId() + "/DCMD/"
			+ descriptor.getEdgeNodeId() + "/" + feature.getDeviceId(), payload, 0, false, USING_COMPRESSION));

	    }

	} else {

	    // TODO store and forward
	    throw new IOException("Not connected - not publishing data");

	}

    }

    public void publishNodeDeath() {

	synchronized (seqLock) {

	    getPayload().setSeq(increaseSeq());
	    executor.execute(new MQTTPublisher(client, NAMESPACE + "/" + getGroupId() + "/NDEATH/" + getEdgeNodeId(),
		    getPayload(), 0, false, false));

	}

    }

    protected void setServerPassword(String password) {

	this.serverPassword = password;

    }

    protected void setServerUrl(String serverUrl) {

	this.serverUrl = serverUrl;

    }

    protected void setServerUsername(String username) {

	this.serverUsername = username;

    }

}
