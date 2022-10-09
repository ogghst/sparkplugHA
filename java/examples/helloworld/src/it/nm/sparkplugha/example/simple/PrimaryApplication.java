package it.nm.sparkplugha.example.simple;

import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import it.nm.sparkplugha.mqtt.MQTTSPHAPrimaryApplication;

public class PrimaryApplication extends MQTTSPHAPrimaryApplication {

    private final static Logger LOGGER = Logger.getLogger(HelloNode.class.getName());

    public static void main(String[] args) throws Exception {

	// LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINE);
	// LogManager.getLogManager().getLogger("").setLevel(Level.FINE);

	LOGGER.fine("Start Primary Application");

	PrimaryApplication node = new PrimaryApplication();
	node.connect();

    }

    public PrimaryApplication() throws Exception {

	super();

	setServerUrl("tcp://localhost:1883");
	setHostId("JavaPrimaryApplication");
	setServerUsername("admin");
	setServerPassword("changeme");

	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {

		new SwingPrimaryApplicationGUI(evtMgr);

	    }

	});

    }

}
