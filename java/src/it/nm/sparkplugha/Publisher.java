/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2020 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package it.nm.sparkplugha;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;
import java.util.zip.Deflater;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.util.CompressionAlgorithm;
import org.eclipse.tahu.util.PayloadUtil;

public class Publisher implements Runnable {

	private final MqttClient client;
	private final String topic;
	private final byte[] bytePayload;
	private final SparkplugBPayload sparkplugPayload;
	private final int qos;
	private final boolean retained;
	private boolean compression = false;
	private CompressionAlgorithm compressionAlgorithm = CompressionAlgorithm.GZIP;
	
	private final static Logger LOGGER = Logger.getLogger(ConnectedSpHANode.class.getName());

	private byte[] compress(byte[] bytes) {

		Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION, true);

		// Set the input for the compressor
		compressor.setInput(bytes);

		// Call the finish() method to indicate that we have
		// no more input for the compressor object
		compressor.finish();

		// Compress the data
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		byte[] readBuffer = new byte[1024];

		while (!compressor.finished()) {
			int readCount = compressor.deflate(readBuffer);
			if (readCount > 0) {
				// Write compressed data to the output stream
				bao.write(readBuffer, 0, readCount);
			}
		}

		// End the compressor
		compressor.end();

		// Return the written bytes from output stream
		return bao.toByteArray();

	}

	public Publisher(MqttClient client, String topic, byte[] bytePayload, int qos, boolean retained,
			boolean compression) {
		this.client = client;
		this.topic = topic;
		this.bytePayload = bytePayload;
		this.sparkplugPayload = null;
		this.qos = qos;
		this.retained = retained;
		this.compression = compression;
	}

	public Publisher(MqttClient client, String topic, SparkplugBPayload sparkplugPayload, int qos, boolean retained,
			boolean compression) {
		this.client = client;
		this.topic = topic;
		this.bytePayload = null;
		this.sparkplugPayload = sparkplugPayload;
		this.qos = qos;
		this.retained = retained;
		this.compression = compression;
	}

	public void run() {
		try {
			publish();
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void publish() throws MqttException, MqttPersistenceException, IOException, SparkplugException {
		if (bytePayload != null) {

			if (compression) {
				client.publish(topic, compress(bytePayload), qos, retained);
			} else {
				client.publish(topic, bytePayload, qos, retained);
			}
			
		} else if (sparkplugPayload != null) {
			sparkplugPayload.setTimestamp(new Date());
			SparkplugBPayloadEncoder encoder = new SparkplugBPayloadEncoder();

			if (compression) {
				client.publish(topic,
						encoder.getBytes(PayloadUtil.compress(sparkplugPayload, compressionAlgorithm)), qos,
						retained);
			} else {
				client.publish(topic, encoder.getBytes(sparkplugPayload), qos, retained);
			}

		} else {
			client.publish(topic, null, 0, false);
		}
	}
}