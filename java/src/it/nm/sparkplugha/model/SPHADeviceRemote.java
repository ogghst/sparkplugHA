package it.nm.sparkplugha.model;

import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.message.model.Topic;

public class SPHADeviceRemote extends SPHADevice {

    public SPHADeviceRemote(Topic topic, SPHANodeRemote node) {

	super(topic.getDeviceId(), node);

    }

    public SPHANodeRemote getNode() {

	return (SPHANodeRemote) super.getNode();

    }

    @Override
    public Template getTemplateDefinition() {

	// TODO Auto-generated method stub
	return null;

    }

    /*
     * @Override public SparkplugBPayload createBirthPayload() {
     * 
     * // TODO Auto-generated method stub return null;
     * 
     * }
     */

    @Override
    public String[] getListeningDeviceDataTopics() {

	// TODO Auto-generated method stub
	return null;

    }

}
