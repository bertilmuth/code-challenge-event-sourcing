package contactlist.boundary.internal.query_handler;

import java.util.function.Function;

import contactlist.boundary.internal.domain.ContactList;
import contactlist.query.FindContacts;

public class HandleFindContacts implements Function<FindContacts, Object> {
	private ContactList contactList;

	public HandleFindContacts(ContactList contactList) {
		this.contactList = contactList;
	}

	@Override
	public Object apply(FindContacts query) {
		return contactList.getContacts();
	}

}
