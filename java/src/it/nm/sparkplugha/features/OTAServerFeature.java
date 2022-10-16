package it.nm.sparkplugha.features;

import static org.eclipse.tahu.message.model.MetricDataType.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.Parameter;
import org.eclipse.tahu.message.model.ParameterDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.message.model.Template.TemplateBuilder;

import it.nm.sparkplugha.SPHANode;
import it.nm.sparkplugha.model.SPHAFeature;
import it.nm.sparkplugha.mqtt.MQTTSPHANode;

public class OTAServerFeature extends SPHAFeature {

    private final static Logger LOGGER = Logger.getLogger(OTAServerFeature.class.getName());

    List<Parameter> params = new ArrayList<Parameter>();
    public static final String FWAVAILABLEMETRIC = "OTAServer/Fw";
    public static final String VERSION = "1.0.0";

    public static final String FWNAMEPROPERTY = "OTAServer/FwName";
    public static final String FWVERSIONPROPERTY = "OTAServer/FwVersion";

    public static final String DEVICETOPIC = "OTA";

    public OTAServerFeature(SPHANode node, String fwName, String fwVersion) throws Exception {

	super("OTAServer", node);
	params.add(new Parameter(FWNAMEPROPERTY, ParameterDataType.String, fwName));
	params.add(new Parameter(FWVERSIONPROPERTY, ParameterDataType.String, fwVersion));

    }

    @Override
    public Template getTemplateDefinition() {

	return new TemplateBuilder().version(VERSION).templateRef(FWAVAILABLEMETRIC).definition(true)
		.addParameters(params).createTemplate();

    }

    @Override
    public void DataArrived(EdgeNodeDescriptor node, Metric metric) throws Exception {

	if (metric.getName().equals(OTAClientFeature.FWREQUESTMETRIC)) {

	    LOGGER.info("Firmware Request Metric arrived");

	    SparkplugBPayload payload = getNode().createPayload();

	    payload.addMetric(new MetricBuilder(FWAVAILABLEMETRIC, Template, new TemplateBuilder().version(VERSION)
		    .templateRef(FWAVAILABLEMETRIC).definition(false).addParameters(params).createTemplate())
		    .createMetric());

	    ((MQTTSPHANode) getNode()).publishFeatureCommand(this, node, payload);

	}

    }

    @Override
    public void CommandArrived(EdgeNodeDescriptor node, Metric metric) throws Exception {

    }

    @Override
    public String getTopic() {

	return DEVICETOPIC;

    }

    String[] listeningDeviceDataTopics = new String[] { OTAClientFeature.DEVICETOPIC };

    @Override
    public String[] getListeningDeviceDataTopics() {

	return listeningDeviceDataTopics;

    }

}
