package it.nm.sparkplugha;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.Parameter;
import org.eclipse.tahu.message.model.ParameterDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.Template.TemplateBuilder;

import it.nm.sparkplugha.model.SPHAFeature;

public class OTAFeature extends SPHAFeature {

    List<Parameter> params = new ArrayList<Parameter>();

    public OTAFeature(BaseSpHANode node, String fwName, String fwVersion) throws SparkplugInvalidTypeException {

	super("OTA", node);
	params.add(new Parameter("FWName", ParameterDataType.String, fwName));
	params.add(new Parameter("FWVersion", ParameterDataType.String, fwVersion));

    }

    public Template getTemplateDefinition() {

	return new TemplateBuilder().version("v1.0").templateRef("OTA").definition(true).addParameters(params)
		.createTemplate();

    }

    public SparkplugBPayload askFirmwarePayload() throws SparkplugInvalidTypeException {

	SparkplugBPayload payload = getNode().createPayload();

	payload.addMetric(new MetricBuilder(getName(), MetricDataType.Template,

		new TemplateBuilder().version("v1.0").templateRef("OTA").definition(false).addParameters(params)
			.createTemplate())
		.createMetric());
	
	return payload;

    }

}
