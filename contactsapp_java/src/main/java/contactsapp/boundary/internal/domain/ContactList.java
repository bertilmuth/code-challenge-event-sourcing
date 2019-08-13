package contactsapp.boundary.internal.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ContactList {
	private List<Contact> contacts;

	public ContactList() {
		this.contacts = new ArrayList<>();
	}

	public boolean existsContact(String contactId) {
		boolean existsContact = contacts.stream().filter(c -> c.getId().equals(contactId)).findFirst().isPresent();
		return existsContact;
	}

	public String newContactId() {
		UUID uuid = UUID.randomUUID();
		String randomUUIDString = uuid.toString();
		return randomUUIDString;
	}

	public void addPerson(String id, String personName) {
		Person person = new Person(id, personName);
		contacts.add(person);
	}

	public void addCompany(String id, String companyName) {
		Company company = new Company(id, companyName);
		contacts.add(company);
	}

	public void renameContact(String contactId, String newName) {
		Optional<Contact> existingContact = contacts.stream().filter(contact -> contact.getId().equals(contactId))
				.findFirst();
		existingContact.ifPresent(c -> c.setName(newName));
	}

	public List<Contact> getContacts() {
		return Collections.unmodifiableList(contacts);
	}
}
