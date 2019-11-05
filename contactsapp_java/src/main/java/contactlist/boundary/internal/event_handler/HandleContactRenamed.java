package contactlist.boundary.internal.event_handler;

import java.util.function.Consumer;

import contactlist.boundary.internal.domain.ContactList;
import contactlist.boundary.internal.event.ContactRenamed;

public class HandleContactRenamed implements Consumer<ContactRenamed> {
	private ContactList contactList;

	public HandleContactRenamed(ContactList contactList) {
		this.contactList = contactList;
	}
	
	@Override
	public void accept(ContactRenamed event) {
		String contactId = event.getContactId();
		String newName = event.getNewName();
		contactList.renameContact(contactId, newName);
	}
}
