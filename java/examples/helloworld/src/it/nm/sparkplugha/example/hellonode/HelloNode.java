package it.nm.sparkplugha.example.hellonode;

import static org.eclipse.tahu.message.model.MetricDataType.String;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.SparkplugBPayload;

import it.nm.sparkplugha.features.OTAClientFeature;
import it.nm.sparkplugha.model.SPHAMetric;
import it.nm.sparkplugha.mqtt.MQTTSPHANode;

public class HelloNode extends MQTTSPHANode {

    private final static Logger LOGGER = Logger.getLogger(HelloNode.class.getName());

    private SPHAMetric helloWorldMetric;

    private OTAClientFeature ota;

    public static void main(String[] args) throws Exception {

	// LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINE);
	// LogManager.getLogManager().getLogger("").setLevel(Level.FINE);

	LOGGER.info("Start HelloNode");

	HelloNode node = new HelloNode();
	node.connect();

	LOGGER.info("Connected");

	Thread.sleep(5000);

	node.askFirmware();

	for (int i = 0; i < 5; i++) {

	    node.run();
	    Thread.sleep(3000);

	}

	node.disconnect();

	LOGGER.info("Disconnected");

    }

    private void askFirmware() throws Exception {

	LOGGER.info("Asking New Firmware");
	publishFeatureData(OTAClientFeature.DEVICETOPIC, ota.askFirmwarePayload());

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

	super.messageArrived(topic, message);

    }

    public HelloNode() throws Exception {

	super();

	setServerUrl("tcp://localhost:1883");
	setGroupId("Sparkplug B Home Automation Devices");
	setEdgeNode("JavaHelloNode");
	setClientId("JavaHelloEdgeNode");
	setServerUsername("admin");
	setServerPassword("changeme");

	ota = new OTAClientFeature(this, "FwName", "1.0.0");
	addFeature(ota);

	helloWorldMetric = createSpHAMetric("helloWorldMetric", String, "uninitialized");
	setNodeBirthPayload(createNodeBirthPayload());

    }

    public void run() throws Exception {

	helloWorldMetric.setValue("Hello, World!");
	updateSpHAMetric(helloWorldMetric);
	publishNodeData(helloWorldMetric.getName());
	LOGGER.info("Sent Hello World");

    }

    @Override
    public void reboot() {

	LOGGER.info("Reboot requested");

    }

    @Override
    public void nextserver() {

	LOGGER.info("Next Server requested");

    }

    @Override
    public void setScanRate(int scanRate) {

	LOGGER.info("Scan Rate requested: " + scanRate);

    }

}
