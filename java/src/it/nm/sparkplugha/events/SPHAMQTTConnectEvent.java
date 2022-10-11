package it.nm.sparkplugha.events;

import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttClient;

public class SPHAMQTTConnectEvent implements SPHAEvent {

    private MqttClient client;

    public SPHAMQTTConnectEvent(MqttClient client) {

	this.client = client;

    }

    @Override
    public Date getTimestamp() {

	// TODO Auto-generated method stub
	return null;

    }

    public MqttClient getClient() {

	return client;

    }

}
