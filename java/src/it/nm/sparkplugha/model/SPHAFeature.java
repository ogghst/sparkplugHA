package it.nm.sparkplugha.model;

import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Template;

import it.nm.sparkplugha.BaseSpHANode;

public abstract class SPHAFeature {

    public SPHAFeature(String name, BaseSpHANode node) {

	this.node = node;
	this.name = name;

    }
    
    public abstract String getTopic();
    public abstract String[] getListeningDeviceDataTopics();
    public abstract String[] getListeningDeviceCommandTopics();

    public String getName() {

	return name;

    }

    public BaseSpHANode getNode() {

	return node;

    }

    private String name;

    private BaseSpHANode node;

    public abstract Template getTemplateDefinition();

    public abstract void DataArrived(Metric metric) throws Exception;

    public abstract void CommandArrived(Metric metric) throws Exception;

}
