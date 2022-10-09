package it.nm.sparkplugha.model;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.MetricDataType;

import java.util.Date;

import org.eclipse.tahu.message.model.MetaData.MetaDataBuilder;
import org.eclipse.tahu.message.model.PropertySet.PropertySetBuilder;

public class SPHAMetric extends Metric {

    protected SPHAMetric(String name, MetricDataType type, Object value) throws SparkplugInvalidTypeException {

	super(name, (long) 0, new Date(), type, false, false, null, null, value);

    }

}
