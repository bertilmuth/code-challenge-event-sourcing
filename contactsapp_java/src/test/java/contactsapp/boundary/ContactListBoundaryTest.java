package contactsapp.boundary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import contactsapp.boundary.internal.domain.Contact;
import contactsapp.boundary.internal.event.CompanyAdded;
import contactsapp.boundary.internal.event.ContactRenamed;
import contactsapp.boundary.internal.event.EmploymentEntered;
import contactsapp.boundary.internal.event.PersonAdded;
import contactsapp.command.AddCompany;
import contactsapp.command.AddPerson;
import contactsapp.command.EnterEmployment;
import contactsapp.command.RenameContact;
import contactsapp.query.FindContacts;
import eventstore.EventStore;

public class ContactListBoundaryTest {
	private static final String MAX_MUSTERMANN = "Max Mustermann";
	private static final String AGILE_COACH = "Agile Coach";

	private static final String FOO_COM = "Foo.com";
	private static final String BAR_COM = "Bar.com";

	private EventStore eventStore;
	private ContactListBoundary boundary;

	@Before
	public void setup() {
		eventStore = new EventStore();
		boundary = new ContactListBoundary(eventStore);
	}
	
	@Test
	public void contact_list_is_initially_empty() {
		Collection<Contact> contacts = findContacts(boundary);
		assertTrue(contacts.isEmpty());
	}

	@Test
	public void adds_person() throws InterruptedException {
		Object handledEvent = addPerson(boundary, MAX_MUSTERMANN);
		assertTrue(handledEvent instanceof PersonAdded);
	}

	@Test
	public void adds_company() throws InterruptedException {
		Object handledEvent = addCompany(boundary, FOO_COM);
		assertTrue(handledEvent instanceof CompanyAdded);
	}

	@Test
	public void renames_contact() throws InterruptedException {
		CompanyAdded companyAdded = (CompanyAdded) addCompany(boundary, FOO_COM);
		String companyId = companyAdded.getCompanyId();

		Object handledEvent = renameContact(boundary, companyId, BAR_COM);
		assertTrue(handledEvent instanceof ContactRenamed);
	}
	
	@Test
	public void person_enters_employment() throws InterruptedException {
		PersonAdded personAdded = (PersonAdded) addPerson(boundary, MAX_MUSTERMANN);
		CompanyAdded companyAdded = (CompanyAdded) addCompany(boundary, FOO_COM);
		
		String personId = personAdded.getPersonId();
		String companyId = companyAdded.getCompanyId();

		Object handledEvent = enterEmployment(personId, companyId, AGILE_COACH, boundary);
		assertTrue(handledEvent instanceof EmploymentEntered);
	}

	@Test
	public void replays_zero_events() {
		ContactListBoundary newContactListBoundary = new ContactListBoundary();
		eventStore.replayWith(newContactListBoundary::reactToEvent);

		Collection<Contact> contacts = findContacts(newContactListBoundary);
		assertTrue(contacts.isEmpty());
	}

	@Test
	public void replays_two_events() {
		addPerson(boundary, MAX_MUSTERMANN);
		addCompany(boundary, BAR_COM);

		ContactListBoundary newContactListBoundary = new ContactListBoundary();
		eventStore.replayWith(newContactListBoundary::reactToEvent);

		Collection<Contact> newContacts = findContacts(newContactListBoundary);
		Iterator<Contact> newContactsIt = newContacts.iterator();
		
		assertEquals(2, newContacts.size());
		assertEquals(MAX_MUSTERMANN, newContactsIt.next().getName());
		assertEquals(BAR_COM, newContactsIt.next().getName());
	}

	@Test
	public void replays_until_after_first_event(){
		addPerson(boundary, MAX_MUSTERMANN);

		Instant afterFirstEvent = Instant.now();
		waitNanoSeconds(1);

		addCompany(boundary, BAR_COM);

		ContactListBoundary newContactListBoundary = new ContactListBoundary();
		eventStore.replayWithUntil(newContactListBoundary::reactToEvent, afterFirstEvent);

		Collection<Contact> newContacts = findContacts(newContactListBoundary);
		assertEquals(1, newContacts.size());
		assertEquals(MAX_MUSTERMANN, newContacts.iterator().next().getName());
	}

	private void waitNanoSeconds(int numberOfNanos) {
		try {
			Thread.sleep(0, numberOfNanos);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private Object addPerson(ContactListBoundary boundary, String personName) {
		AddPerson command = new AddPerson(personName);
		return reactToUserMessage(boundary, command);
	}

	private Object addCompany(ContactListBoundary boundary, String companyName) {
		AddCompany command = new AddCompany(companyName);
		return reactToUserMessage(boundary, command);
	}

	private Object renameContact(ContactListBoundary boundary, String contactId, String newName) {
		RenameContact command = new RenameContact(contactId, newName);
		return reactToUserMessage(boundary, command);
	}

	private Object enterEmployment(String personId, String companyId, String role, ContactListBoundary boundary) {
		EnterEmployment command = new EnterEmployment(personId, companyId, role);
		return reactToUserMessage(boundary, command);
	}
	
	@SuppressWarnings("unchecked")
	private Collection<Contact> findContacts(ContactListBoundary boundary) {
		FindContacts query = new FindContacts();
		return (Collection<Contact>)reactToUserMessage(boundary, query);
	}

	private Object reactToUserMessage(ContactListBoundary boundary, Object message) {
		Object handledEvent;
		try {
			handledEvent = boundary.reactToUserMessage(message).get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return handledEvent;
	}


}
