package it.nm.sparkplugha;

import static org.eclipse.tahu.message.model.MetricDataType.Int64;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.util.CompressionAlgorithm;
import org.eclipse.tahu.util.PayloadUtil;

public class SparkplugHANode implements MqttCallbackExtended {

	// HW/SW versions
	private String hwVersion = "Emulated Hardware";
	private String swVersion = "v1.0.0";

	private static final String NAMESPACE = "spBv1.0";

	// Configuration
	private static final boolean USING_REAL_TLS = false;
	private static final boolean USING_COMPRESSION = false;
	private static final CompressionAlgorithm compressionAlgorithm = CompressionAlgorithm.GZIP;
	private String serverUrl = "tcp://localhost:1883";
	private String groupId = "Sparkplug B Home Automation Devices";
	private String edgeNode = "Java Hello Node";
	private String deviceId = "hello";
	private String clientId = "JavaHelloEdgeNode";
	private String username = "";
	private String password = "";
	private ExecutorService executor;
	private MqttClient client;

	private int bdSeq = 0;
	private int seq = 0;

	// Used to add the birth/death sequence number
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

	byte[] createDeathPayload() throws Exception {
		// Build up DEATH payload - note DEATH payloads don't have a regular sequence
		// number
		SparkplugBPayloadBuilder deathPayload = new SparkplugBPayloadBuilder().setTimestamp(new Date());
		deathPayload = addBdSeqNum(deathPayload);
		byte[] deathBytes;
		if (USING_COMPRESSION) {
			// Compress payload (optional)
			deathBytes = new SparkplugBPayloadEncoder()
					.getBytes(PayloadUtil.compress(deathPayload.createPayload(), compressionAlgorithm));
		} else {
			deathBytes = new SparkplugBPayloadEncoder().getBytes(deathPayload.createPayload());
		}
		return deathBytes;
	}

	public void connect() throws Exception {

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
		options.setUserName(username);
		options.setPassword(password.toCharArray());
		options.setWill(NAMESPACE + "/" + groupId + "/NDEATH/" + edgeNode, createDeathPayload(), 0, false);
		client = new MqttClient(serverUrl, clientId);
		client.setTimeToWait(2000);
		client.setCallback(this); // short timeout on failure to connect
		client.connect(options);

		// Subscribe to control/command messages for both the edge of network node and
		// the attached devices
		client.subscribe(NAMESPACE + "/" + groupId + "/NCMD/" + edgeNode + "/#", 0);
		client.subscribe(NAMESPACE + "/" + groupId + "/DCMD/" + edgeNode + "/#", 0);
		client.subscribe(NAMESPACE + "/#", 0);

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

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		// TODO Auto-generated method stub

	}

	// Used to add the sequence number
	public long getSeqNum() throws Exception {
		System.out.println("seq: " + seq);
		if (seq == 256) {
			seq = 0;
		}
		return seq++;
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

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public MqttClient getClient() {
		return client;
	}

	public void setClient(MqttClient client) {
		this.client = client;
	}

}
