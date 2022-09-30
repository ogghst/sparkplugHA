package it.nm.sparkplugha.model;

import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Template;

public abstract class SPHAFeature {

    public SPHAFeature(String name, SPHANode node) {

	this.node = node;
	this.name = name;

    }
    
    public abstract String getTopic();
    public abstract String[] getListeningDeviceDataTopics();
    public abstract String[] getListeningDeviceCommandTopics();

    public String getName() {

	return name;

    }

    public SPHANode getNode() {

	return node;

    }

    private String name;

    private SPHANode node;

    public abstract Template getTemplateDefinition();

    public abstract void DataArrived(Metric metric) throws Exception;

    public abstract void CommandArrived(Metric metric) throws Exception;

}
