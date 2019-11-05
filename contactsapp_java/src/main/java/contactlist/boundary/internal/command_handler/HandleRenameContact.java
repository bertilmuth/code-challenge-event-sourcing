package contactlist.boundary.internal.command_handler;

import java.util.function.Function;

import contactlist.boundary.internal.domain.ContactList;
import contactlist.boundary.internal.event.ContactRenamed;
import contactlist.boundary.internal.validation.MissingContact;
import contactlist.command.RenameContact;

public class HandleRenameContact implements Function<RenameContact, Object> {
	private ContactList contactList;

	public HandleRenameContact(ContactList contactList) {
		this.contactList = contactList;
	}
	
	@Override
	public Object apply(RenameContact command) {
		String contactId = command.getContactId();
		if(!contactList.isContactPresent(contactId)) {
			return new MissingContact(contactId);
		}
			
		ContactRenamed event = new ContactRenamed(contactId, command.getNewName());
		return event;
	}
}
