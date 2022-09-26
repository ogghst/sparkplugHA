package it.nm.sparkplugha.example.hellonode;

import static org.eclipse.tahu.message.model.MetricDataType.String;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import it.nm.sparkplugha.BaseSpHAMetric;
import it.nm.sparkplugha.ConnectedSpHANode;

public class HelloNode extends ConnectedSpHANode {

	private final static Logger LOGGER = Logger.getLogger(HelloNode.class.getName());

	private BaseSpHAMetric helloWorldMetric;

	public static void main(String[] args) throws Exception {

		//LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINE);
        //LogManager.getLogManager().getLogger("").setLevel(Level.FINE);

		LOGGER.fine("Start HelloNode");
		
		HelloNode node = new HelloNode();
		node.connect();
		node.run();
		node.disconnect();

	}

	public HelloNode() throws Exception {

		super();

		setServerUrl("tcp://localhost:1883");
		setGroupId("Sparkplug B Home Automation Devices");
		setEdgeNode("JavaHelloNode");
		setDeviceId("hello");
		setClientId("JavaHelloEdgeNode");
		setServerUsername("admin");
		setServerPassword("changeme");

		helloWorldMetric = new BaseSpHAMetric("helloWorldMetric", String, "uninitialized");
		addSpHAMetric(helloWorldMetric);
		setNodeBirthPayload(createNodeBirthPayload());

	}

	public void run() throws Exception {
		helloWorldMetric.setValue("Hello, World!");
		updateSpHAMetric(helloWorldMetric);
	}

}
