package it.nm.sparkplugha.model.devices;

import java.util.Date;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.message.model.Template.TemplateBuilder;

public class SPHAHVAC extends Metric {

    public static final String HVACTEMPLATE = "HVAC";
    public static final String VERSION = "1.0.0";
    public static final String SPEED = "Speed";
    public static final String TEMP = "Temp";

    private Template value;
    private Metric speedMetric;
    private Metric tempMetric;

    public SPHAHVAC(String name) throws SparkplugInvalidTypeException {

	super(name, (long) 0, new Date(), MetricDataType.Template, false, false, null, null, null);

	value = new TemplateBuilder().version(VERSION).templateRef(HVACTEMPLATE).definition(true).createTemplate();

	speedMetric = new MetricBuilder(SPEED, MetricDataType.Int16, 0).createMetric();
	value.addMetric(speedMetric);

	tempMetric = new MetricBuilder(TEMP, MetricDataType.Int16, 0).createMetric();
	value.addMetric(tempMetric);

    }


    public void setSpeed(int speed) {

	speedMetric.setValue(speed);

    }

    public void setTemp(int temp) {

	tempMetric.setValue(temp);

    }

}
