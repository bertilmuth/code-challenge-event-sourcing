package eventstore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventStore implements Consumer<Object> {
	private List<TimestampedEvent> storedEvents;
	private List<Consumer<Object>> subscribers;
	
	public EventStore() {
		this.storedEvents = new ArrayList<>();
		this.subscribers = new ArrayList<>();
	}

	public void addSubscriber(Consumer<Object> subscriber) {
		subscribers.add(subscriber);
	}
	
	public void replay() {
		replayWhile(event -> true);
	}
	
	public void replayUntil(Instant instant) {
		replayWhile(event -> eventHappenedUntil(event, instant));
	}
	
	private void replayWhile(Predicate<TimestampedEvent> condition) {
		ArrayList<TimestampedEvent> sortedEvents = new ArrayList<>(storedEvents);
		sortedEvents.sort(Comparator.comparing(TimestampedEvent::getTimestamp));
		
		for (TimestampedEvent sortedEvent : sortedEvents) {
			if(condition.test(sortedEvent)) {
				notifySubscribers(sortedEvent);
			}
		}
	}

	private boolean eventHappenedUntil(TimestampedEvent storedEvent, Instant instant) {
		return !storedEvent.getTimestamp().isAfter(instant);
	}

	@Override
	public void accept(Object event) {
		if(!(event instanceof TimestampedEvent)) {
			// Only timestamped events are forwarded
			return;
		}
		
		TimestampedEvent timestampedEvent = (TimestampedEvent)event;
		storeEvent(timestampedEvent);
		notifySubscribers(timestampedEvent);
	}

	private void storeEvent(TimestampedEvent event) {
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
