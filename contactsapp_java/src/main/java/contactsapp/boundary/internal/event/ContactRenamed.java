package contactsapp.boundary.internal.event;

import eventstore.Event;

public class ContactRenamed extends Event{
	private String contactId;
	private String newName;

	public ContactRenamed(String contactId, String newName) {
		this.contactId = contactId;
		this.newName = newName;
	}

	public String getContactId() {
		return contactId;
	}

	public String getNewName() {
		return newName;
	}
}
