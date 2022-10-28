package it.nm.sparkplugha.model;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Template;

import com.fasterxml.jackson.annotation.JsonValue;

public abstract class SPHADevice {

    // protected Hashtable<String, SPHAMetric> metrics;

    private String deviceId;

    private SPHANode node;

    private SparkplugBPayload payload;

    public abstract Template getTemplateDefinition();

    public SPHADevice(String deviceId, SPHANode node) {

	this.node = node;
	this.deviceId = (deviceId == null ? "<null>" : deviceId);
	// metrics = new Hashtable<String, SPHAMetric>();

    }

    public String getDescriptorString() {

	if (node == null)
	    return "<null>";
	
	return node.getGroupId() + "/" + node.getEdgeNodeId() + "/" + getDeviceId();

    }

    public SparkplugBPayload getPayload() {

	return payload;

    }

    public void setPayload(SparkplugBPayload payload) {

	this.payload = payload;

    }

    // public abstract SparkplugBPayload createBirthPayload() throws
    // SparkplugInvalidTypeException;

    public String getDeviceId() {

	return deviceId;

    }

    public abstract String[] getListeningDeviceDataTopics();

    /*
     * public SPHAMetric createSPHAMetric(String name, MetricDataType dataType,
     * Object initialValue) throws SparkplugInvalidTypeException {
     * 
     * SPHAMetric aMetric = new SPHAMetric(name, dataType, initialValue);
     * metrics.put(aMetric.getName(), aMetric); return aMetric;
     * 
     * }
     * 
     * public SPHAMetric getSPHAMetricByName(String name) {
     * 
     * return metrics.get(name);
     * 
     * }
     * 
     * public Collection<SPHAMetric> getMetrics() {
     * 
     * return metrics.values();
     * 
     * }
     * 
     * public SPHAMetric updateSPHAMetric(SPHAMetric metric) throws Exception {
     * 
     * if (metrics.replace(metric.getName(), metric) == null) {
     * 
     * throw new SpHAMetricNotFoundException("No Metric with name '" +
     * metric.getName() + "', ignoring");
     * 
     * }
     * 
     * return metric;
     * 
     * }
     */

    public SPHANode getNode() {

	return node;

    }

    @Override
    public int hashCode() {

	return this.getDescriptorString().hashCode();

    }

    @Override
    public boolean equals(Object object) {

	if (object instanceof SPHADevice) {

	    return this.getDescriptorString().equals(((SPHADevice) object).getDescriptorString());

	}

	return this.getDescriptorString().equals(object);

    }

    @Override
    @JsonValue
    public String toString() {

	return getDescriptorString();

    }

}
