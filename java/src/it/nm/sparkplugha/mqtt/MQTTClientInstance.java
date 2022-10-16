package it.nm.sparkplugha.mqtt;

import java.util.Vector;
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
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.util.CompressionAlgorithm;

import it.nm.sparkplugha.SPHANodeManager;

public class MQTTClientInstance implements MqttCallback {

    private final static Logger LOGGER = Logger.getLogger(MQTTClientInstance.class.getName());

    private CompressionAlgorithm compressionAlgorithm = CompressionAlgorithm.GZIP;

    private ExecutorService executor;
    private Object seqLock = new Object();
    private String serverPassword = "";
    private String serverUrl = "tcp://localhost:1883";
    private String serverUsername = "";
    private boolean USING_REAL_TLS = false;

    public static final String NAMESPACE = "spBv1.0";

    private static MQTTClientInstance instance;

    private MqttClient client;
    private Vector<MqttCallback> callbacks;

    private MQTTClientInstance() {

	callbacks = new Vector<MqttCallback>();

    }

    public void config(String url, String clientId, String willTopic, byte[] willPayload) throws Exception {

	if (client == null) {

	    client = new MqttClient(url, clientId);
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

	    options.setWill(willTopic, willPayload, 0, false);

	    client = new MqttClient(serverUrl, clientId);
	    client.setTimeToWait(2000);
	    client.setCallback(this); // short timeout on failure to connect
	    client.connect(options);

	    LOGGER.info("connected to '" + serverUrl + "' with ClientID '" + clientId + "'");

	}

    }

    public static MQTTClientInstance getInstance() {

	if (instance == null)
	    instance = new MQTTClientInstance();
	return instance;

    }

    public void connect() throws MqttSecurityException, MqttException {

	if (!client.isConnected())
	    client.connect();

    }

    public void addCallback(MqttCallback callback) {

	callbacks.add(callback);

    }

    @Override
    public void connectionLost(Throwable cause) {

	for (MqttCallback callback : callbacks) {

	    callback.connectionLost(cause);

	}

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

	for (MqttCallback callback : callbacks) {

	    callback.messageArrived(topic, message);

	}

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

	for (MqttCallback callback : callbacks) {

	    callback.deliveryComplete(token);

	}

    }

    public void subscribe(String topic, int qos) throws MqttException {

	client.subscribe(topic, qos);

    }

    public void unsubscribe(String topic) throws MqttException {

	client.unsubscribe(topic);

    }

    public MqttClient getClient() {

	return client;

    }

    public void disconnect() throws MqttException {

	client.disconnect();

    }

}
