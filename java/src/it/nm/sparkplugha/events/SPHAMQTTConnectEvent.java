package it.nm.sparkplugha.events;

import org.eclipse.paho.client.mqttv3.MqttClient;

public class SPHAMQTTConnectEvent implements SPHAEvent {

    private MqttClient client;

    public SPHAMQTTConnectEvent(MqttClient client) {

	this.client = client;

    }

    public MqttClient getClient() {

	return client;

    }

}
