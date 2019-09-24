package eventstore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventStore implements Consumer<Object> {
	private List<Event> storedEvents;
	
	public EventStore() {
		this.storedEvents = new ArrayList<>();
	}
	
	@Override
	public void accept(Object eventObject) {
		storedEvents.add((Event)eventObject);
	}
	
	public void replayWith(Consumer<Object> consumer) {
		replayWithUntil(consumer, Instant.now());
	}
	
	public void replayWithUntil(Consumer<Object> consumer, Instant instant) {
		int index = 0;
		int numberOfStoredEvents = storedEvents.size();
		while(index < numberOfStoredEvents){
			Event storedEvent = storedEvents.get(index);
			if(eventHappenedUntil(storedEvent, instant)) {
				consumer.accept(storedEvent);
				index++;
			} else {
				break;
			}
		}
	}
	
	private boolean eventHappenedUntil(Event storedEvent, Instant instant) {
		return !storedEvent.getTimestamp().isAfter(instant);
	}
}
