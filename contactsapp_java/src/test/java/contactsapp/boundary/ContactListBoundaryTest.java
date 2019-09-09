package contactsapp.boundary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.List;

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
import eventstore.Outbox;

public class ContactListBoundaryTest {
	private static final String MAX_MUSTERMANN = "Max Mustermann";
	private static final String AGILE_COACH = "Agile Coach";

	private static final String FOO_COM = "Foo.com";
	private static final String BAR_COM = "Bar.com";

	private EventStore eventStore;
	private Outbox outbox;
	private ContactListBoundary boundary;

	@Before
	public void setup() {
		eventStore = new EventStore();
		outbox = new Outbox(eventStore);
		boundary = new ContactListBoundary(outbox);
		eventStore.addSubscriber(boundary::reactToEvent);
	}
	
	@Test
	public void contact_list_is_initially_empty() {
		List<Contact> contacts = findContacts(boundary);
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
	public void renaming_missing_contact_fails() throws InterruptedException {
		Object handledEvent = renameContact(boundary, BAR_COM, BAR_COM);
		assertNull(handledEvent);
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
		ContactListBoundary newContactListBoundary = new ContactListBoundary(outbox);
		eventStore.addSubscriber(newContactListBoundary::reactToEvent);
		eventStore.replay();

		List<Contact> contacts = findContacts(newContactListBoundary);
		assertTrue(contacts.isEmpty());
	}

	@Test
	public void replays_two_events() {
		addPerson(boundary, MAX_MUSTERMANN);
		addCompany(boundary, BAR_COM);

		ContactListBoundary newContactListBoundary = new ContactListBoundary(outbox);
		eventStore.addSubscriber(newContactListBoundary::reactToEvent);
		eventStore.replay();

		List<Contact> newContacts = findContacts(newContactListBoundary);
		assertEquals(2, newContacts.size());
		assertEquals(MAX_MUSTERMANN, newContacts.get(0).getName());
		assertEquals(BAR_COM, newContacts.get(1).getName());
	}

	@Test
	public void replays_until_after_first_event() throws InterruptedException {
		addPerson(boundary, MAX_MUSTERMANN);

		Instant afterFirstEvent = Instant.now();
		waitNanoSecond();

		addCompany(boundary, BAR_COM);

		ContactListBoundary newContactListBoundary = new ContactListBoundary(outbox);
		eventStore.addSubscriber(newContactListBoundary::reactToEvent);
		eventStore.replayUntil(afterFirstEvent);

		List<Contact> newContacts = findContacts(newContactListBoundary);
		assertEquals(1, newContacts.size());
		assertEquals(MAX_MUSTERMANN, newContacts.get(0).getName());
	}

	private void waitNanoSecond() throws InterruptedException {
		Thread.sleep(0, 1);
	}

	private Object addPerson(ContactListBoundary boundary, String personName) {
		AddPerson command = new AddPerson(personName);
		return reactToCommand(boundary, command);
	}

	private Object addCompany(ContactListBoundary boundary, String companyName) {
		AddCompany command = new AddCompany(companyName);
		return reactToCommand(boundary, command);
	}

	private Object renameContact(ContactListBoundary boundary, String contactId, String newName) {
		RenameContact command = new RenameContact(contactId, newName);
		return reactToCommand(boundary, command);
	}

	private Object enterEmployment(String personId, String companyId, String role, ContactListBoundary boundary) {
		EnterEmployment command = new EnterEmployment(personId, companyId, role);
		return reactToCommand(boundary, command);
	}

	private Object reactToCommand(ContactListBoundary boundary, Object command) {
		boundary.reactToCommand(command);
		Object handledEvent = boundary.getHandledEvent();
		return handledEvent;
	}

	private List<Contact> findContacts(ContactListBoundary boundary) {
		@SuppressWarnings("unchecked")
		List<Contact> contacts = (List<Contact>) boundary.reactToQuery(new FindContacts()).get();
		return contacts;
	}
}
