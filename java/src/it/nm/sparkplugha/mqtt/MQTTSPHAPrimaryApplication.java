package it.nm.sparkplugha.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
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
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.tahu.SparkplugParsingException;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.util.CompressionAlgorithm;
import org.eclipse.tahu.util.TopicUtil;

import it.nm.sparkplugha.SPHANode;
import it.nm.sparkplugha.SPHANodeRemote;
import it.nm.sparkplugha.SPHANode.SPHANodeState;
import it.nm.sparkplugha.events.SPHADeviceBirthEvent;
import it.nm.sparkplugha.events.SPHADeviceCommandEvent;
import it.nm.sparkplugha.events.SPHADeviceDataEvent;
import it.nm.sparkplugha.events.SPHAEventManager;
import it.nm.sparkplugha.events.SPHAMQTTConnectEvent;
import it.nm.sparkplugha.events.SPHAMQTTConnectLossEvent;
import it.nm.sparkplugha.events.SPHANodeBirthEvent;
import it.nm.sparkplugha.events.SPHANodeCommandEvent;
import it.nm.sparkplugha.events.SPHANodeDataEvent;
import it.nm.sparkplugha.events.SPHANodeDeathEvent;
import it.nm.sparkplugha.events.SPHANodeOutOfSequenceEvent;

public class MQTTSPHAPrimaryApplication implements MqttCallbackExtended {

    private class RebirthDelayTask extends TimerTask {

	private EdgeNodeDescriptor edgeNodeDescriptor;

	public RebirthDelayTask(EdgeNodeDescriptor edgeNodeDescriptor) {

	    this.edgeNodeDescriptor = edgeNodeDescriptor;

	}

	public void run() {

	    if (rebirthTimers.get(edgeNodeDescriptor) != null) {

		rebirthTimers.get(edgeNodeDescriptor).cancel();
		rebirthTimers.remove(edgeNodeDescriptor);

	    }

	}

    }

    private static final String HOST_NAMESPACE = "STATE";
    private final static Logger LOGGER = Logger.getLogger(MQTTSPHAPrimaryApplication.class.getName());
    public static final String NAMESPACE = "spBv1.0";
    protected MqttClient client;
    private String hostId = "undefinedHostId";
    private CompressionAlgorithm compressionAlgorithm = CompressionAlgorithm.GZIP;
    private final Map<EdgeNodeDescriptor, SPHANode> edgeNodeMap;
    private ExecutorService executor;
    private final Map<EdgeNodeDescriptor, Timer> rebirthTimers;
    public SPHAEventManager evtMgr;

    private Object seqLock = new Object();

    private String serverPassword = "";

    private String serverUrl = "tcp://localhost:1883";

    private String serverUsername = "";

    private boolean USING_COMPRESSION = false;

    private boolean USING_REAL_TLS = false;

    public MQTTSPHAPrimaryApplication() {

	edgeNodeMap = new ConcurrentHashMap<>();
	rebirthTimers = new ConcurrentHashMap<>();
	evtMgr = new SPHAEventManager();

    }

    public void connect() {

	try {

	    // Thread pool for outgoing published messages
	    executor = Executors.newFixedThreadPool(1);

	    // Build up Host Will payload
	    byte[] willPayload = "OFFLINE".getBytes();

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

	    if (hostId != null && !hostId.isEmpty()) {

		options.setWill(HOST_NAMESPACE + "/" + hostId, willPayload, 1, true);

	    }

	    client = new MqttClient(serverUrl, hostId);
	    client.setTimeToWait(2000);
	    client.setCallback(this); // short timeout on failure to connect
	    client.connect(options);

	    // Subscribe to control/command messages for both the edge of network node and
	    // the attached devices
	    client.subscribe(NAMESPACE + "/#", 0);

	    if (hostId != null && !hostId.isEmpty()) {

		client.subscribe(HOST_NAMESPACE + "/" + hostId, 0);

	    }

	} catch (Exception e) {

	    e.printStackTrace();

	}

    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

	evtMgr.trigger(new SPHAMQTTConnectEvent(client));
	LOGGER.info("Connected! - publishing birth");
	publishHostBirth();

    }

    @Override
    public void connectionLost(Throwable cause) {

	evtMgr.trigger(new SPHAMQTTConnectLossEvent(client, cause));
	LOGGER.log(Level.SEVERE, "The MQTT Connection was lost! - will auto-reconnect", cause);

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

	try {

	    LOGGER.fine("Published message: " + Arrays.toString(token.getTopics()) + " - " +

		    new String(token.getMessage().getPayload(), StandardCharsets.UTF_8));

	} catch (Exception e) {

	    LOGGER.fine("Published message: " + Arrays.toString(token.getTopics()) + " - unparseable payload");

	}

    }

    public String getHostId() {

	return hostId;

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

    private boolean handleSeqNumberCheck(SPHANode edgeNode, long incomingSeqNum, Topic topic) {

	if (topic.getType().equals(MessageType.NBIRTH)) {

	    if (incomingSeqNum != 0) {

		LOGGER.severe("birth sequence of node '" + edgeNode.getDescriptorString() + "' is " + incomingSeqNum
			+ ", expecting 0. node offline and rebirth is necessary");
		return false;

	    }

	    edgeNode.setSeq(incomingSeqNum);
	    return true;

	}

	// Get the last stored sequence number
	long storedSeqNum = edgeNode.getSeq();
	// Conditionally wrap to 0
	long expectedSeqNum = storedSeqNum + 1 == 256 ? 0 : storedSeqNum + 1;

	// Check if current sequence number is valid
	if (incomingSeqNum != expectedSeqNum) {

	    LOGGER.severe("sequence of node '" + edgeNode.getDescriptorString() + "' is " + incomingSeqNum
		    + ", expecting " + expectedSeqNum + ". node offline and rebirth is necessary");
	    return false;

	} else {

	    edgeNode.setSeq(incomingSeqNum);
	    return true;

	}

    }

    @Override
    public void messageArrived(String stringTopic, MqttMessage message) throws Exception {

	LOGGER.fine("message arrived on topic '" + stringTopic + "'");

	if (stringTopic != null && stringTopic.startsWith(NAMESPACE)) {

	    // Get the topic tokens
	    String[] sparkplugTokens = stringTopic.split("/");

	    // Parse the Topic
	    Topic topic;

	    try {

		topic = TopicUtil.parseTopic(sparkplugTokens);

	    } catch (SparkplugParsingException e) {

		LOGGER.log(Level.SEVERE, "Error parsing topic", e);
		return;

	    }

	    if (topic.isType(MessageType.NCMD) || topic.isType(MessageType.DCMD)) {

		LOGGER.fine("Ignoring CMD message");
		return;

	    }

	    // Get the payload
	    SparkplugBPayloadDecoder decoder = new SparkplugBPayloadDecoder();

	    SparkplugBPayload payload;

	    try {

		payload = decoder.buildFromByteArray(message.getPayload());

	    } catch (Exception e) {

		LOGGER.log(Level.SEVERE, "Error decoding payload", e);
		return;

	    }

	    // Get the EdgeNodeDescriptor
	    EdgeNodeDescriptor edgeNodeDescriptor = new EdgeNodeDescriptor(topic.getGroupId(), topic.getEdgeNodeId());

	    SPHANode edgeNode = edgeNodeMap.get(edgeNodeDescriptor);

	    // Special case for NBIRTH
	    if (topic.getType().equals(MessageType.NBIRTH)) {

		edgeNode = new SPHANodeRemote(topic.getGroupId(), topic.getEdgeNodeId(), SPHANodeState.ONLINE, payload);
		edgeNodeMap.put(edgeNodeDescriptor, edgeNode);

	    }

	    // Special case for NDEATH
	    if (topic.getType().equals(MessageType.NDEATH)) {

		edgeNode = new SPHANodeRemote(topic.getGroupId(), topic.getEdgeNodeId(), SPHANodeState.OFFLINE,
			payload);
		edgeNodeMap.put(edgeNodeDescriptor, edgeNode);

	    }

	    // Failed to handle the message
	    if (edgeNode == null) {

		LOGGER.warning("Unexpected message on topic " + topic + " - requesting Rebirth");
		requestRebirth(edgeNodeDescriptor);
		return;

	    }

	    if (topic.getType().equals(MessageType.NDEATH)) {

		edgeNodeMap.get(edgeNodeDescriptor).setState(SPHANodeState.OFFLINE);
		evtMgr.trigger(new SPHANodeDeathEvent(edgeNode));
		return;

	    }

	    // Check the sequence number
	    if (handleSeqNumberCheck(edgeNode, payload.getSeq(), topic)) {

		LOGGER.fine("Validated sequence number on topic: " + topic);

		// now inject the payload to the node
		edgeNode.setPayload(payload);

		if (topic.getType().equals(MessageType.NBIRTH)) {

		    evtMgr.trigger(new SPHANodeBirthEvent(edgeNode));

		}

		if (topic.getType().equals(MessageType.DBIRTH)) {

		    evtMgr.trigger(new SPHADeviceBirthEvent(edgeNode));

		}

		if (topic.getType().equals(MessageType.NDATA)) {

		    evtMgr.trigger(new SPHANodeDataEvent(edgeNode));

		}

		if (topic.getType().equals(MessageType.DDATA)) {

		    evtMgr.trigger(new SPHADeviceDataEvent(edgeNode));

		}

		if (topic.getType().equals(MessageType.NCMD)) {

		    evtMgr.trigger(new SPHANodeCommandEvent(edgeNode));

		}

		if (topic.getType().equals(MessageType.DCMD)) {

		    evtMgr.trigger(new SPHADeviceCommandEvent(edgeNode));

		}

	    } else {

		requestRebirth(edgeNodeDescriptor);

	    }

	} else if (stringTopic != null && stringTopic.startsWith(HOST_NAMESPACE)) {

	    if ("OFFLINE".equals(new String(message.getPayload()))) {

		LOGGER.warning("The MQTT Server incorrectly reported the primary host is offline - correcting");
		publishHostBirth();

	    }

	} else {

	    LOGGER.fine("Ignoring non-Sparkplug messages");

	}

    }

    private void publishHostBirth() {

	try {

	    if (hostId != null && !hostId.isEmpty()) {

		LOGGER.info("Publishing Host Birth");
		executor.execute(new MQTTPublisher(client, HOST_NAMESPACE + "/" + hostId, "ONLINE".getBytes(), 1, true,
			USING_COMPRESSION));

	    }

	} catch (Exception e) {

	    e.printStackTrace();

	}

    }

    private void requestRebirth(EdgeNodeDescriptor edgeNodeDescriptor) {

	try {

	    Timer rebirthDelayTimer = rebirthTimers.get(edgeNodeDescriptor);

	    if (rebirthDelayTimer == null) {

		LOGGER.info("Requesting Rebirth from " + edgeNodeDescriptor);
		rebirthDelayTimer = new Timer();
		rebirthTimers.put(edgeNodeDescriptor, rebirthDelayTimer);
		rebirthDelayTimer.schedule(new RebirthDelayTask(edgeNodeDescriptor), 5000);

		SPHANode edgeNode = edgeNodeMap.get(edgeNodeDescriptor);

		if (edgeNode != null) {

		    // Set the Edge Node offline
		    edgeNode.setState(SPHANodeState.OFFLINE);
		    evtMgr.trigger(new SPHANodeOutOfSequenceEvent(edgeNode));

		} else {

		    LOGGER.warning("Node is not in the internal list");

		}

		// Request a device rebirth
		String rebirthTopic = new Topic(NAMESPACE, edgeNodeDescriptor.getGroupId(),
			edgeNodeDescriptor.getEdgeNodeId(), MessageType.NCMD).toString();
		SparkplugBPayload rebirthPayload = new SparkplugBPayloadBuilder().setTimestamp(new Date())
			.addMetric(
				new MetricBuilder("Node Control/Rebirth", MetricDataType.Boolean, true).createMetric())
			.createPayload();

		executor.execute(new MQTTPublisher(client, rebirthTopic, rebirthPayload, 0, false, USING_COMPRESSION));

	    } else {

		LOGGER.info("Not requesting Rebirth since we have in the last 5 seconds");

	    }

	} catch (Exception e) {

	    LOGGER.log(Level.SEVERE, "Failed to create Rebirth request. Cause: " + e.getMessage(),
		    e.fillInStackTrace());
	    return;

	}

    }

    public void setHostId(String clientId) {

	this.hostId = clientId;

    }

    public void setServerPassword(String serverPassword) {

	this.serverPassword = serverPassword;

    }

    public void setServerUrl(String serverUrl) {

	this.serverUrl = serverUrl;

    }

    public void setServerUsername(String serverUsername) {

	this.serverUsername = serverUsername;

    }

}
