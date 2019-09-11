package eventstore;

import java.util.HashSet;
import java.util.function.Consumer;

import org.requirementsascode.EventQueue;

public class Outbox implements Consumer<Object> {
	private EventStore eventStore;
	private final EventQueue outbox;
	private final HashSet<Object> unpublishedEvents;
                                                                     
	public Outbox(EventStore eventStore) {
		this.eventStore = eventStore;
		this.outbox = new EventQueue(this::publishEventsAtLeastOnce);
		this.unpublishedEvents = new HashSet<>();
	}
	
	private void publishEventsAtLeastOnce(Object eventObject) {
		for (Object unpublishedEvent : unpublishedEvents) {
			outbox.put(unpublishedEvent);
		}
		unpublishedEvents.add(eventObject);
		eventStore.accept(eventObject);
		unpublishedEvents.remove(eventObject);
	}

	@Override
	public void accept(Object eventObject) {
		outbox.put(eventObject);		
	}
}
