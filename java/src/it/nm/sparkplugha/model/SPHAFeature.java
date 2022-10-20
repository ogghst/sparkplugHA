package it.nm.sparkplugha.model;

import java.util.Collection;
import java.util.Hashtable;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.message.model.Topic;

import com.fasterxml.jackson.annotation.JsonValue;

import it.nm.sparkplugha.exceptions.SpHAMetricNotFoundException;

public abstract class SPHAFeature {

    //protected Hashtable<String, SPHAMetric> metrics;

    private String name;

    private SPHANode node;

    private SparkplugBPayload payload;

    public abstract Template getTemplateDefinition();

    public SPHAFeature(String name, SPHANode node, SparkplugBPayload payload) {

	this.node = node;
	this.name = name;
	this.payload = payload;
	//metrics = new Hashtable<String, SPHAMetric>();

    }

    public abstract String getTopic();

    public abstract String[] getListeningDeviceDataTopics();

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

    public String getName() {

	return name;

    }

    public SPHANode getNode() {

	return node;

    }

    @Override
    public int hashCode() {

	return this.getTopic().hashCode();

    }

    @Override
    public boolean equals(Object object) {

	if (object instanceof SPHAFeature) {

	    return this.getTopic().equals(((SPHAFeature) object).getTopic());

	}

	return this.getTopic().equals(object);

    }

    @Override
    @JsonValue
    public String toString() {

	return getTopic();

    }

}
