package it.nm.sparkplugha.test;

import static org.eclipse.tahu.message.model.MetricDataType.String;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayload;

import it.nm.sparkplugha.BaseSpHAMetric;
import it.nm.sparkplugha.ConnectedSparkplugHANode;

public class HelloNode extends ConnectedSparkplugHANode {

	private BaseSpHAMetric aStringMetric;

	public static void main(String[] args) throws Exception {

		LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINE);

		HelloNode node = new HelloNode();
		node.connect();
		node.run();
		node.disconnect();

	}

	public HelloNode() throws Exception {

		super();

		setServerUrl("tcp://localhost:1883");
		setGroupId("Sparkplug B Home Automation Devices");
		setEdgeNode("Java Hello Node");
		setDeviceId("hello");
		setClientId("JavaHelloEdgeNode");
		setUsername("admin");
		setPassword("changeme");

		aStringMetric = new BaseSpHAMetric("aStringMetricName", String, "aStringMetricValue");
		addSpHAMetric(aStringMetric);
		setNodeBirthPayload(createNodeBirthPayload());

	}

	public void run() throws Exception {
		aStringMetric.setValue("aStringMetricValue");
		publishSpHAMetric(aStringMetric.getName());
	}

}
