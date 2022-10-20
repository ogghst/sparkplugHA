package it.nm.sparkplugha.example.simple;

import static org.eclipse.tahu.message.model.MetricDataType.String;

import java.util.logging.Logger;

import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.SparkplugBPayload;

import it.nm.sparkplugha.features.OTAClientFeature;
import it.nm.sparkplugha.model.devices.SPHAHVAC;
import it.nm.sparkplugha.mqtt.MQTTSPHANode;

public class HelloNode extends MQTTSPHANode {

    private final static Logger LOGGER = Logger.getLogger(HelloNode.class.getName());

    private Metric helloWorldMetric;

    private OTAClientFeature ota;

    private SPHAHVAC hvac;

    private void askFirmware() throws Exception {

	LOGGER.info("Asking New Firmware");
	publishFeatureData(ota, ota.askFirmwarePayload());

    }

    public HelloNode() throws Exception {

	super("SparkplugHA", "JavaHelloNode", SPHANodeState.OFFLINE);

	setServerUrl("tcp://localhost:1883");

	setClientId("JavaHelloEdgeNode");
	setServerUsername("admin");
	setServerPassword("changeme");

	ota = new OTAClientFeature(this, "FwName", "1.0.0");
	addFeature(ota);

	// hvac = new SPHAHVAC("HelloHVAC");
	// addDevice(hvac);

	helloWorldMetric = createMetric("helloWorldMetric", String, "uninitialized");

    }

    public void run() throws Exception {

	SparkplugBPayload payload = createPayload();
	helloWorldMetric.setValue("Hello, World! - Message " + (++count));
	payload.addMetric(helloWorldMetric);
	publishNodeData(payload);
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

	for (int i = 0; i < 5; i++) {

	    Thread.sleep(4000);
	    node.run();

	}

	node.askFirmware();

	Thread.sleep(4000);

	node.disconnect();

	LOGGER.info("Disconnected");

    }

}
