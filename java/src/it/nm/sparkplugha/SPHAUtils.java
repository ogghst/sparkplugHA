package it.nm.sparkplugha;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;

public class SPHAUtils {

    public static final String SCADA_NAMESPACE = "SCADA";
    
    public static final Metric addMetric(String name, MetricDataType type, Object value) throws SparkplugInvalidTypeException {

	Metric metric = new MetricBuilder(name, type, value).createMetric();
	return metric;

    }
    

    public static final String newUUID() {

	return java.util.UUID.randomUUID().toString();

    }

}
