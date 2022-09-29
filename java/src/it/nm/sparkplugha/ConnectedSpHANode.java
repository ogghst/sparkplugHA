package it.nm.sparkplugha;

import java.io.IOException;
import java.util.Date;
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
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.util.CompressionAlgorithm;
import org.eclipse.tahu.util.PayloadUtil;

import it.nm.sparkplugha.model.SPHAFeature;

public abstract class ConnectedSpHANode extends BaseSpHANode implements MqttCallbackExtended {

    private final static Logger LOGGER = Logger.getLogger(ConnectedSpHANode.class.getName());

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

	options.setWill(NAMESPACE + "/" + groupId + "/NDEATH/" + edgeNode, payloadToBytes(createNodeDeathPayload()), 0,
		false);

	client = new MqttClient(serverUrl, clientId);
	client.setTimeToWait(2000);
	client.setCallback(this); // short timeout on failure to connect
	client.connect(options);

	LOGGER.info("connected to '" + serverUrl + "' with ClientID '" + clientId + "'");

	// Subscribe to control/command messages for both the edge of network node and
	// the attached devices
	client.subscribe(NAMESPACE + "/" + groupId + "/NCMD/" + edgeNode + "/#", 0);
	client.subscribe(NAMESPACE + "/" + groupId + "/DCMD/" + edgeNode + "/#", 0);
	// client.subscribe(NAMESPACE + "/#", 0);
	client.subscribe(Utils.SCADA_NAMESPACE + "/#", 0);

    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

	LOGGER.fine("Connection estabilished with '" + serverURI + "', reconnect = " + reconnect);
	publishNodeBirth(getNodeBirthPayload());

    }

    @Override
    public void connectionLost(Throwable cause) {

	LOGGER.log(Level.WARNING, "Connection lost. Cause: " + cause.toString(), cause);

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

	LOGGER.fine("Published message: " + token.getTopics());

    }

    protected void disconnect() throws Exception {

	client.unsubscribe(NAMESPACE + "/" + groupId + "/NCMD/" + edgeNode + "/#");
	// client.unsubscribe(NAMESPACE + "/" + groupId + "/DCMD/" + edgeNode + "/#");
	// client.unsubscribe(NAMESPACE + "/#");

	new MQTTPublisher(client, NAMESPACE + "/" + groupId + "/NDEATH/" + edgeNode, createNodeDeathPayload(), 1, true,
		USING_COMPRESSION).publish();

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

	if (splitTopic[0].equals(Utils.SCADA_NAMESPACE)) {

	    LOGGER.fine("SCADA update: " + splitTopic[1]);
	    return;

	}

	SparkplugBPayloadDecoder decoder = new SparkplugBPayloadDecoder();
	SparkplugBPayload inboundPayload = decoder.buildFromByteArray(message.getPayload());

	// Debug
	for (Metric metric : inboundPayload.getMetrics()) {

	    LOGGER.fine("	Metric " + metric.getName() + "=" + metric.getValue());

	}

	if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) && splitTopic[2].equals("NCMD")
		&& splitTopic[3].equals(edgeNode)) {

	    for (Metric metric : inboundPayload.getMetrics()) {

		if ("Node Control/Rebirth".equals(metric.getName()) && ((Boolean) metric.getValue())) {

		    publishNodeBirth(getNodeBirthPayload());

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

	else if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) && splitTopic[2].equals("DCMD")
		&& splitTopic[3].equals(edgeNode)) {

	    for (Metric metric : inboundPayload.getMetrics()) {

		// forward to features
		for (SPHAFeature feature : features.values()) {

		    feature.CommandArrived(metric);

		}

	    }

	}

	else if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) && splitTopic[2].equals("DCMD")
		&& splitTopic[3].equals(edgeNode)) {

	    LOGGER.fine("Command received for device " + splitTopic[4]);

	    for (Metric metric : inboundPayload.getMetrics()) {

		String name = metric.getName();
		Object value = metric.getValue();

		SPHAMetric spHAMetric = getSpHAMetricByName(name);

		if (spHAMetric == null) {

		    LOGGER.warning("No Metric with name '" + name + "', ignoring");

		} else {

		    updateSpHAMetric(spHAMetric);
		    publishNodeData(name);

		}

	    }

	} else if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) && splitTopic[2].equals("DDATA")
		&& splitTopic[3].equals(edgeNode)) {

	    for (Metric metric : inboundPayload.getMetrics()) {

		// forward to features
		for (SPHAFeature feature : features.values()) {

		    feature.DataArrived(metric);

		}

	    }

	}

    }

    protected void publishNodeBirth(SparkplugBPayload payload) {

	synchronized (seqLock) {

	    // Reset the sequence number
	    resetSeqNum();
	    executor.execute(new MQTTPublisher(client, NAMESPACE + "/" + groupId + "/NBIRTH/" + edgeNode, payload, 0,
		    false, USING_COMPRESSION));

	}

    }

    public void publishNodeData(String metricName) throws Exception {

	publishNodeData(createSpHaMetricPayload(metricName));

    }

    @Override
    public void publishNodeData(SparkplugBPayload payload)
	    throws MqttPersistenceException, MqttException, IOException, SparkplugException {

	if (client.isConnected()) {

	    synchronized (seqLock) {

		executor.execute(new MQTTPublisher(client, NAMESPACE + "/" + groupId + "/NDATA/" + edgeNode, payload, 0,
			false, USING_COMPRESSION));

	    }

	} else {

	    // TODO store and forward
	    throw new IOException("Not connected - not publishing data");

	}

    }

    @Override
    public void publishNodeCommand(SparkplugBPayload payload) throws Exception {

	if (client.isConnected()) {

	    synchronized (seqLock) {

		executor.execute(new MQTTPublisher(client, NAMESPACE + "/" + groupId + "/NCMD/" + edgeNode, payload, 0,
			false, USING_COMPRESSION));

	    }

	} else {

	    // TODO store and forward
	    throw new IOException("Not connected - not publishing data");

	}

    }

    @Override
    public void publishFeatureData(SparkplugBPayload payload) throws Exception {

	if (client.isConnected()) {

	    synchronized (seqLock) {

		executor.execute(new MQTTPublisher(client, NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode, payload, 0,
			false, USING_COMPRESSION));

	    }

	} else {

	    // TODO store and forward
	    throw new IOException("Not connected - not publishing data");

	}

    }

    @Override
    public void publishFeatureCommand(SparkplugBPayload payload) throws Exception {

	if (client.isConnected()) {

	    synchronized (seqLock) {

		executor.execute(new MQTTPublisher(client, NAMESPACE + "/" + groupId + "/DCMD/" + edgeNode, payload, 0,
			false, USING_COMPRESSION));

	    }

	} else {

	    // TODO store and forward
	    throw new IOException("Not connected - not publishing data");

	}

    }

    protected void publishNodeDeath(SparkplugBPayload payload) {

	synchronized (seqLock) {

	    // Reset the sequence number
	    resetSeqNum();
	    executor.execute(new MQTTPublisher(client, NAMESPACE + "/" + groupId + "/NDEATH/" + edgeNode, payload, 0,
		    false, false));

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
