package it.nm.sparkplugha.features;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.Parameter;
import org.eclipse.tahu.message.model.ParameterDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.message.model.Template.TemplateBuilder;

import it.nm.sparkplugha.model.SPHADeviceLocal;
import it.nm.sparkplugha.model.SPHANodeLocal;

public class OTAClientFeature extends SPHADeviceLocal {

    private final static Logger LOGGER = Logger.getLogger(OTAClientFeature.class.getName());

    List<Parameter> params = new ArrayList<Parameter>();
    public static final String FWREQUESTMETRIC = "OTAClient/Fw";
    public static final String VERSION = "1.0.0";

    public static final String FWNAMEPROPERTY = "OTAClient/FwName";
    public static final String FWVERSIONPROPERTY = "OTAClient/FwVersion";

    public static final String DEVICETOPIC = "OTA";

    public OTAClientFeature(SPHANodeLocal node, String fwName, String fwVersion) throws SparkplugInvalidTypeException {

	super("OTAClient", node);
	params.add(new Parameter(FWNAMEPROPERTY, ParameterDataType.String, fwName));
	params.add(new Parameter(FWVERSIONPROPERTY, ParameterDataType.String, fwVersion));
	setPayload(createBirthPayload());

    }

    @Override
    public Template getTemplateDefinition() {

	return new TemplateBuilder().version(VERSION).templateRef(FWREQUESTMETRIC).definition(true)
		.addParameters(params).createTemplate();

    }

    public SparkplugBPayload createBirthPayload() throws SparkplugInvalidTypeException {

	SparkplugBPayload payload = getNode().createPayload();
	
	payload.addMetric(
		new MetricBuilder(FWREQUESTMETRIC, MetricDataType.Template, getTemplateDefinition()).createMetric());
	
	return payload;

    }

    public SparkplugBPayload askFirmwarePayload() throws SparkplugInvalidTypeException {

	SparkplugBPayload payload = getNode().createPayload();

	payload.addMetric(new MetricBuilder(FWREQUESTMETRIC, MetricDataType.Template,

		new TemplateBuilder().version(VERSION).templateRef(FWREQUESTMETRIC).definition(false)
			.addParameters(params).createTemplate())
		.createMetric());

	return payload;

    }

    @Override
    public void DataArrived(EdgeNodeDescriptor node, Metric metric) {

    }

    @Override
    public void CommandArrived(EdgeNodeDescriptor node, Metric metric) {

	if (metric.getName().equals(OTAServerFeature.FWAVAILABLEMETRIC)) {

	    LOGGER.info("New Firmware Available from node " + node.getGroupId() + "/" + node.getEdgeNodeId());

	    for (Parameter p : ((Template) metric.getValue()).getParameters()) {

		LOGGER.info("   Parameter Name: '" + p.getName() + "', value = '" + p.getValue() + "'");

	    }

	}

    }

    @Override
    public String getDeviceId() {

	return DEVICETOPIC;

    }

    String[] listeningDeviceDataTopics = new String[] {};

    @Override
    public String[] getListeningDeviceDataTopics() {

	return listeningDeviceDataTopics;

    }

}
