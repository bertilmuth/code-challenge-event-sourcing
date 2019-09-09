package eventstore;

import java.util.function.Consumer;

public class Outbox implements Consumer<Object> {
	private EventStore eventStore;

	public Outbox(EventStore eventStore) {
		this.eventStore = eventStore;
	}
	
	@Override
	public void accept(Object eventObject) {
		eventStore.accept(eventObject);
	}

}
