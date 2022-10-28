package it.nm.sparkplugha.model;

import java.util.Collection;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.SparkplugBPayload;

import com.fasterxml.jackson.annotation.JsonValue;

public abstract class SPHANode {

    private final static Logger LOGGER = Logger.getLogger(SPHANode.class.getName());

    protected static final String NAMESPACE = "spBv1.0";
    protected static final String NODE_CONTROL_SCAN_RATE = "Node Control/Scan Rate";
    protected static final String NODE_CONTROL_NEXT_SERVER = "Node Control/Next Server";
    protected static final String NODE_CONTROL_REBOOT = "Node Control/Reboot";
    protected static final String NODE_CONTROL_REBIRTH = "Node Control/Rebirth";
    protected static final String BD_SEQ = "bdSeq";

    protected int bdSeq = 0;
    protected long seq = 0;

    public enum SPHANodeState {
	OFFLINE, ONLINE
    }

    private SPHANodeState state;

    protected String groupId;
    protected String edgeNodeId;

    protected String hwVersion = "Emulated Hardware";
    protected String swVersion = "v1.0.0";
    
    private SparkplugBPayload payload;

    //private Hashtable<String, SPHAMetric> metrics;
    private Hashtable<String, SPHADevice> devices;

    public SPHANode(String groupId, String edgeNodeId, SPHANodeState state) {

	this.groupId = groupId;
	this.edgeNodeId = edgeNodeId;
	this.state = state;
	this.seq = 0;
	this.bdSeq = 0;
	this.setPayload(new SparkplugBPayload());

	//metrics = new Hashtable<String, SPHAMetric>();
	devices = new Hashtable<String, SPHADevice>();

    }
    
    public SPHANode(String groupId, String edgeNodeId, SPHANodeState state, SparkplugBPayload payload) {
	
	this.groupId = groupId;
	this.edgeNodeId = edgeNodeId;
	this.state = state;
	this.seq = 0;
	this.bdSeq = 0;
	this.setPayload(payload);

	//metrics = new Hashtable<String, SPHAMetric>();
	devices = new Hashtable<String, SPHADevice>();
	
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

    public long increaseSeq() {

	if (seq == 256) {

	    seq = -1;

	}

	seq = seq + 1;
	LOGGER.fine("Increased seq : "+seq);
	
	return seq;

    }

    public long getSeq() {

	LOGGER.fine("Get seq : "+seq);
	return seq;

    }

    public void setSeq(long seq) {

	this.seq = seq;

    }

    public long resetSeq() {

	seq = 0;
	return seq;

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

    protected void addDevice(SPHADevice device) {

	devices.put(device.getDeviceId(), device);

    }
    
    

    public void setState(SPHANodeState state) {

	LOGGER.fine("State: " + state);
	this.state = state;

    }

    public SPHANodeState getState() {

	return state;

    }

    public Collection<SPHADevice> getDevices() {

	return devices.values();

    }
    
    public SPHADevice getDevice(String name) {
	return devices.get(name);
    }

    /*
    public SPHAMetric createSPHAMetric(String name, MetricDataType dataType, Object initialValue)
	    throws SparkplugInvalidTypeException {

	SPHAMetric aMetric = new SPHAMetric(name, dataType, initialValue);
	metrics.put(aMetric.getName(), aMetric);
	return aMetric;

    }

    public SPHAMetric getSPHAMetricByName(String name) {

	return metrics.get(name);

    }

    public Collection<SPHAMetric> getMetrics() {

	return metrics.values();

    }

    public SPHAMetric updateSPHAMetric(SPHAMetric metric) throws Exception {

	if (metrics.replace(metric.getName(), metric) == null) {

	    throw new SpHAMetricNotFoundException("No Metric with name '" + metric.getName() + "', ignoring");

	}

	return metric;

    }
    */

    @Override
    public int hashCode() {

	return this.getDescriptorString().hashCode();

    }

    @Override
    public boolean equals(Object object) {

	if (object instanceof SPHANode) {

	    return this.getDescriptorString().equals(((SPHANode) object).getDescriptorString());

	}

	return this.getDescriptorString().equals(object);

    }

    @Override
    @JsonValue
    public String toString() {

	return getDescriptorString();

    }

    public SparkplugBPayload getPayload() {

	return payload;

    }

    public void setPayload(SparkplugBPayload payload) {

	this.payload = payload;

    }

}
