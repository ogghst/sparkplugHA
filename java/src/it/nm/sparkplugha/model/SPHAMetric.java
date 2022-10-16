package it.nm.sparkplugha.model;

import java.util.Date;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.MetricDataType;

public class SPHAMetric extends Metric {

    public SPHAMetric(String name, MetricDataType type, Object value) throws SparkplugInvalidTypeException {

	super(name, (long) 0, new Date(), type, false, false, null, null, value);

    }

}
