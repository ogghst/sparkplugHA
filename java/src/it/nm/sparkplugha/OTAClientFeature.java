package it.nm.sparkplugha;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.Parameter;
import org.eclipse.tahu.message.model.ParameterDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.Template.TemplateBuilder;

import it.nm.sparkplugha.model.SPHAFeature;

public class OTAClientFeature extends SPHAFeature {

    List<Parameter> params = new ArrayList<Parameter>();
    public static final String OTACLIENTREQUESTMETRIC = "OTA/Request";
    public static final String OTACLIENTVERSION = "1.0.0";

    public OTAClientFeature(BaseSpHANode node, String fwName, String fwVersion) throws SparkplugInvalidTypeException {

	super("OTA", node);
	params.add(new Parameter("FWName", ParameterDataType.String, fwName));
	params.add(new Parameter("FWVersion", ParameterDataType.String, fwVersion));

    }

    @Override
    public Template getTemplateDefinition() {

	return new TemplateBuilder().version(OTACLIENTVERSION).templateRef(OTACLIENTREQUESTMETRIC).definition(true).addParameters(params)
		.createTemplate();

    }

    public SparkplugBPayload askFirmwarePayload() throws SparkplugInvalidTypeException {

	SparkplugBPayload payload = getNode().createPayload();

	payload.addMetric(new MetricBuilder(getName(), MetricDataType.Template,

		new TemplateBuilder().version(OTACLIENTVERSION).templateRef(OTACLIENTREQUESTMETRIC).definition(false).addParameters(params)
			.createTemplate())
		.createMetric());
	
	return payload;

    }

    @Override
    public void DataArrived(Metric metric) {

	if(metric.getName() == OTAServerFeature.OTASERVERFWLISTMETRIC) {
	    
	}

    }

    @Override
    public void CommandArrived(Metric metric) {

    }

}
