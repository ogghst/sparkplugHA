package it.nm.sparkplugha;

import static org.eclipse.tahu.message.model.MetricDataType.Int64;
import static org.eclipse.tahu.message.model.MetricDataType.Template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Parameter;
import org.eclipse.tahu.message.model.ParameterDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.Template.TemplateBuilder;

import it.nm.sparkplugha.model.SPHAFeature;

public class OTAServerFeature extends SPHAFeature {

    List<Parameter> params = new ArrayList<Parameter>();
    public static final String OTASERVERFWLISTMETRIC = "OTAServer/FwList";
    public static final String OTASERVERERSION = "1.0.0";

    public static final String OTASERVERFWNAME = "OTAServer/FwName";
    public static final String OTASERVERFWVERSION = "OTAServer/FwVersion";

    public OTAServerFeature(String name, BaseSpHANode node, String fwName, String fwVersion) throws Exception {

	super(name, node);
	params.add(new Parameter(OTASERVERFWNAME, ParameterDataType.String, fwName));
	params.add(new Parameter(OTASERVERFWVERSION, ParameterDataType.String, fwVersion));

    }

    @Override
    public Template getTemplateDefinition() {

	return new TemplateBuilder().version(OTASERVERERSION).templateRef(OTASERVERFWLISTMETRIC).definition(true)
		.addParameters(params).createTemplate();

    }

    @Override
    public void DataArrived(Metric metric) throws Exception {

	if (metric.getName() == OTAClientFeature.OTACLIENTREQUESTMETRIC) {

	    SparkplugBPayload payload = getNode().createPayload();

	    payload.addMetric(new MetricBuilder("TemplateInst", Template, new TemplateBuilder().version(OTASERVERERSION)
		    .templateRef(OTASERVERFWLISTMETRIC).definition(false).addParameters(params).createTemplate())
		    .createMetric());

	    getNode().publishFeatureCommand(payload);

	}

    }

    @Override
    public void CommandArrived(Metric metric) {

	// TODO Auto-generated method stub

    }

}
