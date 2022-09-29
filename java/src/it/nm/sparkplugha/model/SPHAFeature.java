package it.nm.sparkplugha.model;

import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Template;

import it.nm.sparkplugha.BaseSpHANode;

public abstract class SPHAFeature {

    public SPHAFeature(String name, BaseSpHANode node) {

	this.node = node;
	this.name = name;

    }

    public String getName() {

	return name;

    }

    public void setName(String name) {

	this.name = name;

    }

    private String name;
    private BaseSpHANode node;

    public abstract Template getTemplateDefinition();

    public BaseSpHANode getNode() {

	return node;

    }

    public abstract void DataArrived(Metric metric) throws Exception;

    public abstract void CommandArrived(Metric metric) throws Exception;

}
