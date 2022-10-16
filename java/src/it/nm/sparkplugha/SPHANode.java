package it.nm.sparkplugha;

import static org.eclipse.tahu.message.model.MetricDataType.Int64;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;

import com.fasterxml.jackson.annotation.JsonValue;

import it.nm.sparkplugha.exceptions.SpHAMetricNotFoundException;
import it.nm.sparkplugha.model.SPHAFeature;
import it.nm.sparkplugha.model.SPHAMetric;

public class SPHANode {

    public static final String NODE_CONTROL_SCAN_RATE = "Node Control/Scan Rate";
    public static final String NODE_CONTROL_NEXT_SERVER = "Node Control/Next Server";
    public static final String NODE_CONTROL_REBOOT = "Node Control/Reboot";
    public static final String NODE_CONTROL_REBIRTH = "Node Control/Rebirth";
    public static final String BD_SEQ = "bdSeq";

    protected String hwVersion = "Emulated Hardware";
    protected String swVersion = "v1.0.0";

    private int bdSeq = 0;
    private long seq = 0;

    protected Hashtable<String, SPHAMetric> metrics;
    protected Hashtable<String, SPHAFeature> features;

    private SPHANodeState state;

    public enum SPHANodeState {
	OFFLINE, ONLINE
    }

    private final String groupId;
    private final String edgeNodeId;

    @Override
    public int hashCode() {

	return this.getDescriptorString().hashCode();

    }

    @Override
    public boolean equals(Object object) {

	if (object instanceof EdgeNodeDescriptor) {

	    return this.getDescriptorString().equals(((EdgeNodeDescriptor) object).getDescriptorString());

	}

	return this.getDescriptorString().equals(object);

    }

    @Override
    @JsonValue
    public String toString() {

	return getDescriptorString();

    }

    private final static Logger LOGGER = Logger.getLogger(SPHANode.class.getName());

    public SPHANode(String groupId, String edgeNodeId) {

	metrics = new Hashtable<String, SPHAMetric>();
	features = new Hashtable<String, SPHAFeature>();
	this.groupId = groupId;
	this.edgeNodeId = edgeNodeId;
	this.state = SPHANodeState.OFFLINE;
	this.bdSeq = 0;
	this.seq = 0;

    }

    public void addFeature(SPHAFeature feature) {

	features.put(feature.getName(), feature);

    }

    public Collection<SPHAFeature> getFeatures() {

	return features.values();

    }
    
    public void addMetric(SPHAMetric metric) {

	metrics.put(metric.getName(), metric);

    }

    public Collection<SPHAMetric> getMetrics() {

	return metrics.values();

    }



    public SPHAMetric getSPHAMetricByName(String name) {

	return metrics.get(name);

    }

    public SPHAMetric updateSPHAMetric(SPHAMetric metric) throws Exception {

	if (metrics.replace(metric.getName(), metric) == null) {

	    throw new SpHAMetricNotFoundException("No Metric with name '" + metric.getName() + "', ignoring");

	}

	return metric;

    }

    public long increaseSeq() {

	if (seq == 256) {

	    seq = 0;

	}

	return ++seq;

    }

    public long getSeq() {

	return seq;

    }

    public void setSeq(long seq) {

	this.seq = seq;

    }

    public long resetSeq() {

	seq = 0;
	return seq;

    }

    private String newUUID() {

	return java.util.UUID.randomUUID().toString();

    }

    public String getGroupId() {

	return groupId;

    }

    public String getEdgeNodeId() {

	return edgeNodeId;

    }

    public String getDescriptorString() {

	return groupId + "/" + edgeNodeId;

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

    public void setState(SPHANodeState state) {

	LOGGER.fine("State: " + state);
	this.state = state;

    }

    public SPHANodeState getState() {

	return state;

    }

    public int getBdSeq() {

	return bdSeq;

    }

    public void setBdSeq(int bdSeq) {

	this.bdSeq = bdSeq;

    }

}