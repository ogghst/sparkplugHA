package it.nm.sparkplugha;

import java.io.IOException;

import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.util.PayloadUtil;

import it.nm.sparkplugha.model.SPHAMetric;

public class Utils {

    public static final String SCADA_NAMESPACE = "SCADA";

    public static SPHAMetric createSPHAMetric(String name, MetricDataType dataType, Object initialValue)
	    throws SparkplugInvalidTypeException {

	SPHAMetric aMetric = new SPHAMetric(name, dataType, initialValue);
	return aMetric;

    }
    

    public static byte[] payloadToBytes(SparkplugBPayload payload, CompressionAlgorithm compressionAlgorithm) throws IOException, SparkplugException {

	byte[] bytes;

	if (compressionAlgorithm != null) {

	    // Compress payload (optional)
	    bytes = new SparkplugBPayloadEncoder().getBytes(PayloadUtil.compress(payload, compressionAlgorithm));

	} else {

	    bytes = new SparkplugBPayloadEncoder().getBytes(payload);

	}

	return bytes;

    }


}
