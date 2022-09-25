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
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.util.CompressionAlgorithm;
import org.eclipse.tahu.util.PayloadUtil;

public class ConnectedSparkplugHANode extends BaseSparkplugHANode implements MqttCallbackExtended {

	private boolean USING_REAL_TLS = false;
	private boolean USING_COMPRESSION = false;
	private CompressionAlgorithm compressionAlgorithm = CompressionAlgorithm.GZIP;
	private MqttClient client;
	private String serverUrl = "tcp://localhost:1883";
	private String username = "";
	private String password = "";
	private ExecutorService executor;

	private final static Logger LOGGER = Logger.getLogger(ConnectedSparkplugHANode.class.getName());

	private Object seqLock = new Object();

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

		options.setWill(NAMESPACE + "/" + groupId + "/NDEATH/" + edgeNode,
				getBytes(createNodeDeathPayload().createPayload()), 0, false);
		client = new MqttClient(serverUrl, clientId);
		client.setTimeToWait(2000);
		client.setCallback(this); // short timeout on failure to connect
		client.connect(options);

		LOGGER.info("connected to '" + serverUrl + "' with ClientID '" + clientId + "'");

		// Subscribe to control/command messages for both the edge of network node and
		// the attached devices
		client.subscribe(NAMESPACE + "/" + groupId + "/NCMD/" + edgeNode + "/#", 0);
		client.subscribe(NAMESPACE + "/" + groupId + "/DCMD/" + edgeNode + "/#", 0);
		client.subscribe(NAMESPACE + "/#", 0);

	}
	
	public void disconnect() throws Exception {
		client.unsubscribe(NAMESPACE + "/" + groupId + "/NCMD/" + edgeNode + "/#");
		client.unsubscribe(NAMESPACE + "/" + groupId + "/DCMD/" + edgeNode + "/#");
		client.unsubscribe(NAMESPACE + "/#");
		
		publish(NAMESPACE + "/" + groupId + "/NDEATH/" + edgeNode, createNodeDeathPayload().createPayload());

		client.disconnect();
	}

	private byte[] getBytes(SparkplugBPayload payload) throws IOException, SparkplugException {

		byte[] bytes;
		if (USING_COMPRESSION) {
			// Compress payload (optional)
			bytes = new SparkplugBPayloadEncoder().getBytes(PayloadUtil.compress(payload, compressionAlgorithm));
		} else {
			bytes = new SparkplugBPayloadEncoder().getBytes(payload);
		}
		return bytes;

	}

	public void sendDataPayload(SparkplugBPayload payload)
			throws MqttPersistenceException, MqttException, IOException, SparkplugException {

		if (client.isConnected()) {
			synchronized (seqLock) {

				// Compress payload (optional)
				if (USING_COMPRESSION) {
					client.publish(NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode + "/" + deviceId,
							new SparkplugBPayloadEncoder()
									.getBytes(PayloadUtil.compress(payload, compressionAlgorithm)),
							0, false);
				} else {
					client.publish(NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode + "/" + deviceId,
							new SparkplugBPayloadEncoder().getBytes(payload), 0, false);
				}
			}
		} else {
			// TODO store and forward
			throw new IOException("Not connected - not publishing data");
		}

	}

	public void sendNodeBirth(SparkplugBPayload payload) {

		synchronized (seqLock) {
			// Reset the sequence number
			resetSeqNum();
			executor.execute(new Publisher(NAMESPACE + "/" + groupId + "/NBIRTH/" + edgeNode, payload));
		}

	}
	
	public void sendNodeDeath(SparkplugBPayload payload) {

		synchronized (seqLock) {
			// Reset the sequence number
			resetSeqNum();
			executor.execute(new Publisher(NAMESPACE + "/" + groupId + "/NDEATH/" + edgeNode, payload));
		}

	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		LOGGER.fine("Message Arrived on topic " + topic);

		SparkplugBPayloadDecoder decoder = new SparkplugBPayloadDecoder();
		SparkplugBPayload inboundPayload = decoder.buildFromByteArray(message.getPayload());

		// Debug
		for (Metric metric : inboundPayload.getMetrics()) {
			LOGGER.fine("Metric " + metric.getName() + "=" + metric.getValue());
		}

		String[] splitTopic = topic.split("/");
		if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) && splitTopic[2].equals("NCMD")
				&& splitTopic[3].equals(edgeNode)) {
			for (Metric metric : inboundPayload.getMetrics()) {
				if ("Node Control/Rebirth".equals(metric.getName()) && ((Boolean) metric.getValue())) {
					sendNodeBirth(getNodeBirthPayload());
				} else {
					LOGGER.warning("Unknown Node Command NCMD: " + metric.getName());
				}
			}
		} else if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) && splitTopic[2].equals("DCMD")
				&& splitTopic[3].equals(edgeNode)) {
			LOGGER.fine("Command recevied for device " + splitTopic[4]);

			for (Metric metric : inboundPayload.getMetrics()) {
				String name = metric.getName();
				Object value = metric.getValue();

				BaseSpHAMetric spHAMetric = getSpHAMetricByName(name);
				if (spHAMetric == null) {
					LOGGER.warning("No Metric with name '" + name + "', ignoring");
				} else {
					updateSpHAMetricValue(name, value);
					publishSpHAMetric(name);
				}

			}

		}
	}

	public void publishSpHAMetric(String name) throws Exception {
		
		LOGGER.fine("publishing metric '"+name+"'");

		SparkplugBPayload outboundPayload = createSpHaMetricPayload(name);
		executor.execute(
				new Publisher(NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode + "/" + deviceId, outboundPayload));

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		LOGGER.fine("Published message: " + token);

	}

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		sendNodeBirth(getNodeBirthPayload());

	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
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

	private void publish(String topic, SparkplugBPayload outboundPayload) throws MqttException, MqttPersistenceException, IOException, SparkplugException {

		for (Metric metric : outboundPayload.getMetrics()) {
			LOGGER.fine("publish metric '"+metric.getName()+"', value '"+metric.getValue().toString()+"'");
		}
		
		outboundPayload.setTimestamp(new Date());
		SparkplugBPayloadEncoder encoder = new SparkplugBPayloadEncoder();

		// Compress payload (optional)
		if (USING_COMPRESSION) {
			client.publish(topic, encoder.getBytes(PayloadUtil.compress(outboundPayload, compressionAlgorithm)),
					0, false);
		} else {
			client.publish(topic, encoder.getBytes(outboundPayload), 0, false);
		}
	}

	private class Publisher implements Runnable {

		private String topic;
		private SparkplugBPayload outboundPayload;

		public Publisher(String topic, SparkplugBPayload outboundPayload) {
			this.topic = topic;
			this.outboundPayload = outboundPayload;
		}

		public void run() {
			try {
				publish(topic, outboundPayload);
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
