package contactsapp.boundary.internal.command_handler;

import java.util.UUID;

class IdGenerator {
	public static String newId() {
		UUID uuid = UUID.randomUUID();
		String randomUUIDString = uuid.toString();
		return randomUUIDString;
	}
}
