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
import it.nm.sparkplugha.features.OTAServerFeature;
import it.nm.sparkplugha.model.SPHAMetric;
import it.nm.sparkplugha.mqtt.MQTTSPHANode;

public class OTAServerNode extends MQTTSPHANode {

    private final static Logger LOGGER = Logger.getLogger(OTAServerNode.class.getName());

    private SPHAMetric helloWorldMetric;

    private OTAServerFeature ota;

    public static void main(String[] args) throws Exception {

	// LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINE);
	// LogManager.getLogManager().getLogger("").setLevel(Level.FINE);

	LOGGER.info("Start HelloOTAServerode");

	OTAServerNode node = new OTAServerNode();
	node.connect();

	LOGGER.info("Connected");

	while (true)
	    Thread.sleep(10000);

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

	super.messageArrived(topic, message);

    }

    public OTAServerNode() throws Exception {

	super();

	setServerUrl("tcp://localhost:1883");
	setGroupId("Sparkplug B Home Automation Devices");
	setEdgeNode("JavaHelloOTAServer");
	setClientId("JavaHelloOTAServerEdgeNode");
	setServerUsername("admin");
	setServerPassword("changeme");

	ota = new OTAServerFeature(this, "fwName", "1.0.0");
	addFeature(ota);
	setNodeBirthPayload(createNodeBirthPayload());

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
