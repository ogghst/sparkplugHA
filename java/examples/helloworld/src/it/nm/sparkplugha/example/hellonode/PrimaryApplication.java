package it.nm.sparkplugha.example.hellonode;

import static org.eclipse.tahu.message.model.MetricDataType.String;

import java.util.logging.Logger;

import it.nm.sparkplugha.ConnectedSpHAPrimaryApplication;
import it.nm.sparkplugha.SPHAMetric;

public class PrimaryApplication extends ConnectedSpHAPrimaryApplication {

	private final static Logger LOGGER = Logger.getLogger(HelloNode.class.getName());

	public static void main(String[] args) throws Exception {

		//LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINE);
        //LogManager.getLogManager().getLogger("").setLevel(Level.FINE);

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

	}
	
}
