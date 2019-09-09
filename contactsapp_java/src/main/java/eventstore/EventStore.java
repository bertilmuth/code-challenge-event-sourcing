package eventstore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventStore implements Consumer<Object> {
	private List<Event> storedEvents;
	private List<Consumer<Object>> subscribers;
	
	public EventStore() {
		this.storedEvents = new ArrayList<>();
		this.subscribers = new ArrayList<>();
	}

	public void addSubscriber(Consumer<Object> subscriber) {
		subscribers.add(subscriber);
	}
	
	public void replay() {
		project(event -> true);
	}
	
	public void replayUntil(Instant instant) {
		project(event -> eventHappenedUntil(event, instant));
	}
	
	public void project(Predicate<Event> condition) {
		ArrayList<Event> sortedEvents = new ArrayList<>(storedEvents);
		sortedEvents.sort(Comparator.comparing(Event::getTimestamp));
		
		for (Event sortedEvent : sortedEvents) {
			if(condition.test(sortedEvent)) {
				notifySubscribers(sortedEvent);
			}
		}
	}

	private boolean eventHappenedUntil(Event storedEvent, Instant instant) {
		return !storedEvent.getTimestamp().isAfter(instant);
	}

	@Override
	public void accept(Object event) {
		if(!(event instanceof Event)) {
			// Only timestamped events are forwarded
			return;
		}
		
		Event timestampedEvent = (Event)event;
		storeEvent(timestampedEvent);
		notifySubscribers(timestampedEvent);
	}

	private void storeEvent(Event event) {
		storedEvents.add(event);
	}
	
	private void notifySubscribers(Object event) {
		for (Consumer<Object> subscriber : subscribers) {
			notifySubscriber(event, subscriber);
		}
	}

	private void notifySubscriber(Object event, Consumer<Object> subscriber) {
		subscriber.accept(event);
	}
}
