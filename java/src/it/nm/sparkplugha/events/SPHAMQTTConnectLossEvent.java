package it.nm.sparkplugha.events;

import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttClient;

public class SPHAMQTTConnectLossEvent implements SPHAEvent {

    private MqttClient client;
    private Throwable cause;

    public SPHAMQTTConnectLossEvent(MqttClient client, Throwable cause) {

	this.client = client;
	this.cause = cause;

    }

    @Override
    public Date getTimestamp() {

	// TODO Auto-generated method stub
	return null;

    }

    public MqttClient getClient() {

	return client;

    }

    public Throwable getCause() {

	return cause;

    }
    
    @Override
    public String toString() {

	String clientStr = "<no client>";
	if (client != null) clientStr = client.getClientId();
	String causeStr = "<no cause>";
	if (cause != null) causeStr = cause.toString();
        return clientStr +" connect loss - reason: "+causeStr;
    
    }

}
