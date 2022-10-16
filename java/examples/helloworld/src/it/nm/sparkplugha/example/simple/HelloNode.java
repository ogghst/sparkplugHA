package it.nm.sparkplugha.example.simple;

import static org.eclipse.tahu.message.model.MetricDataType.String;

import java.util.logging.Logger;

import it.nm.sparkplugha.features.OTAClientFeature;
import it.nm.sparkplugha.model.SPHAMetric;
import it.nm.sparkplugha.model.devices.SPHAHVAC;
import it.nm.sparkplugha.mqtt.MQTTSPHANode;

public class HelloNode extends MQTTSPHANode {

    private final static Logger LOGGER = Logger.getLogger(HelloNode.class.getName());

    private SPHAMetric helloWorldMetric;

    private OTAClientFeature ota;

    private SPHAHVAC hvac;

    private void askFirmware() throws Exception {

	LOGGER.info("Asking New Firmware");
	publishFeatureData(ota, ota.askFirmwarePayload());

    }

    public HelloNode() throws Exception {

	super("SparkplugHA", "JavaHelloNode", SPHANodeState.OFFLINE, null);

	setServerUrl("tcp://localhost:1883");

	setClientId("JavaHelloEdgeNode");
	setServerUsername("admin");
	setServerPassword("changeme");

	ota = new OTAClientFeature(this, "FwName", "1.0.0");
	addFeature(ota);

	// hvac = new SPHAHVAC("HelloHVAC");
	// addDevice(hvac);

	helloWorldMetric = createSPHAMetric("helloWorldMetric", String, "uninitialized");
	setNodeBirthPayload(createNodeBirthPayload());

    }

    public void run() throws Exception {

	helloWorldMetric.setValue("Hello, World! - Message " + (++count));
	publishNodeData(createSPHAMetricPayload(updateSPHAMetric(helloWorldMetric)));
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

    private int count = 0;

    public static void main(String[] args) throws Exception {

	LOGGER.info("Start HelloNode");

	HelloNode node = new HelloNode();
	node.connect();

	LOGGER.info("Connected");

	Thread.sleep(5000);

	node.askFirmware();

	for (int i = 0; i < 60; i++) {

	    Thread.sleep(3000);
	    node.run();

	}

	node.disconnect();

	LOGGER.info("Disconnected");

    }

}
