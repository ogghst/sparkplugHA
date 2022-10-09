package it.nm.sparkplugha.model.devices;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.message.model.Template.TemplateBuilder;

import it.nm.sparkplugha.model.SPHAMetric;

public class SPHAHVAC extends SPHAMetric {

    public static final String HVACTEMPLATE = "HVAC";
    public static final String VERSION = "1.0.0";
    public static final String SPEED = "Speed";
    public static final String TEMP = "Temp";

    private Template value;
    private Metric speedMetric;
    private Metric tempMetric;

    public SPHAHVAC(String name) throws SparkplugInvalidTypeException {

	super(name, MetricDataType.Template, null);

	value = new TemplateBuilder().version(VERSION).templateRef(HVACTEMPLATE).definition(true).createTemplate();

	speedMetric = new MetricBuilder(SPEED, MetricDataType.Int16, 0).createMetric();
	value.addMetric(speedMetric);

	tempMetric = new MetricBuilder(TEMP, MetricDataType.Int16, 0).createMetric();
	value.addMetric(tempMetric);

    }

    @Override
    public Object getValue() {

	return value;

    }

    public void setSpeed(int speed) {

	speedMetric.setValue(new Integer(speed));

    }

    public void setTemp(int temp) {

	tempMetric.setValue(new Integer(temp));

    }

}
