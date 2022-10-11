package it.nm.sparkplugha.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

public class SPHAEventManager {

    private final static Logger LOGGER = Logger.getLogger(SPHAEventManager.class.getName());

    Map<Class<SPHAEvent>, List<SPHAEventListener>> listeners = new HashMap<>();

    public SPHAEventManager() {

    }

    public <T> void subscribe(Class<T> eventType, SPHAEventListener listener) {

	List<SPHAEventListener> users = listeners.get(eventType);

	if (users == null) {

	    users = new Vector<SPHAEventListener>();
	    listeners.put((Class<SPHAEvent>) eventType, users);

	}

	users.add(listener);

	LOGGER.fine("subscribed: " + eventType);

    }

    public <T> void unsubscribe(Class<T> eventType, SPHAEventListener listener) {

	List<SPHAEventListener> users = listeners.get(eventType);
	if (users == null)
	    return;
	users.remove(listener);

	LOGGER.fine("unsubscribed: " + eventType);

    }

    public void trigger(SPHAEvent event) {

	List<SPHAEventListener> users = listeners.get(event.getClass());
	if (users == null)
	    return;

	LOGGER.fine("trigger - class: "+event.getClass().getCanonicalName()+" - name: " + event);

	for (SPHAEventListener listener : users) {

	    listener.trigger(event);

	}

    }

}
