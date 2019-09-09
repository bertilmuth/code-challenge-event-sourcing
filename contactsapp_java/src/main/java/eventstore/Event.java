package eventstore;

import java.time.Instant;
import java.util.UUID;

public abstract class Event {
	private UUID uuid;
	private Instant timestamp;
	
	public Event() {
		this.uuid = UUID.randomUUID();
		this.timestamp = Instant.now();
	}
	
	public UUID getUuid() {
		return uuid;
	}

	public Instant getTimestamp() {
		return timestamp;
	}
}
