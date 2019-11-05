package contactlist.boundary.internal.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ContactList {
	private Map<String, Contact> contactIdToContactMap;

	public ContactList() {
		this.contactIdToContactMap = new LinkedHashMap<>();
	}

	public String newId() {
		UUID uuid = UUID.randomUUID();
		String randomUUIDString = uuid.toString();
		return randomUUIDString;
	}

	public void addPerson(String id, String personName) {
		Person person = new Person(id, personName);
		addContact(person);
	}

	public void addCompany(String id, String companyName) {
		Company company = new Company(id, companyName);
		addContact(company);
	}
	
	private void addContact(Contact contact) {
		String id = contact.getId();
		contactIdToContactMap.put(id, contact);
	}

	public void renameContact(String contactId, String newName) {
		Optional<Contact> optionalContact = getContact(contactId);
		if(optionalContact.isPresent()) {
			final Contact contact = optionalContact.get();
			contact.setName(newName);
		} else {
			throw new RuntimeException("No contact wit id " + contactId + " found!");
		}
	}

	public void enterEmployment(String personId, String companyId, String role) {
		Person person = (Person) getContact(personId).get();
		Company company = (Company) getContact(companyId).get();
		Employment employment = new Employment(person, company, role);
		person.setEmployment(employment);
	}

	public boolean isContactPresent(String contactId) {
		Optional<Contact> contact = getContact(contactId);
		boolean existsContact = contact.isPresent();
		return existsContact;
	}

	public Optional<Contact> getContact(String contactId) {
		Contact nullableContact = contactIdToContactMap.get(contactId);
		return Optional.ofNullable(nullableContact);
	}

	public Collection<Contact> getContacts() {
		Collection<Contact> values = contactIdToContactMap.values();
		return Collections.unmodifiableCollection(values);
	}
}
