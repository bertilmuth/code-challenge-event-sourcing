package main;

import java.util.Collection;

import contactlist.boundary.ContactListBoundary;
import contactlist.boundary.internal.domain.Contact;
import contactlist.boundary.internal.event.PersonAdded;
import contactlist.command.AddCompany;
import contactlist.command.AddPerson;
import contactlist.command.RenameContact;
import contactlist.query.FindContacts;
import eventstore.EventStore;

public class ContactsApp {
	private static final String PERSON_NAME = "John Q. Public";
	private static final String NEW_PERSON_NAME = "John Q. Private";
	private static final String COMPANY_NAME = "FooBar Inc.";
	
	public static void main(String[] args) throws InterruptedException {
		new ContactsApp().start();
	}

	private void start() throws InterruptedException {
		EventStore eventStore = new EventStore();
		ContactListBoundary boundary = new ContactListBoundary(eventStore);

		for (int i = 0; i < 100000; i++) {
			final EventSender eventSender = new EventSender(boundary);
			Thread eventSenderThread = new Thread(eventSender);
			eventSenderThread.start();
		}
		
		Thread.sleep(10000);
		int numberOfStoredEvents = eventStore.getNumberOfStoredEvents();
		
		System.out.println("Replaying events...");
		ContactListBoundary newBoundary = new ContactListBoundary();
		long before = System.currentTimeMillis();
		eventStore.replayWith(newBoundary::reactToEvent);
		long after = System.currentTimeMillis();
		long passed = after-before;

		System.out.println("\nThe contacts are:");
		Collection<Contact> contacts = findContacts(newBoundary);
		printToConsole(contacts);

		System.out.println("Replay took " + passed + " milliseconds for " + numberOfStoredEvents + " events.");
		
		boundary.stop();
		newBoundary.stop();		
	}
	
	@SuppressWarnings("unchecked")
	private Collection<Contact> findContacts(ContactListBoundary boundary) {
		FindContacts query = new FindContacts();
		try {
			return (Collection<Contact>)boundary.reactToMessage(query).get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void printToConsole(Collection<Contact> contacts) {
		for (Contact contact : contacts) {
			System.out.println(contact);
		}
	}

	private static class EventSender implements Runnable {
		private ContactListBoundary boundary;

		public EventSender(ContactListBoundary boundary) {
			this.boundary = boundary;
		}

		@Override
		public void run() {
			String personId = addPerson(PERSON_NAME, boundary);

			addCompany(COMPANY_NAME, boundary);

			renameContact(personId, NEW_PERSON_NAME, boundary);	
		}

		private String addPerson(String personName, ContactListBoundary boundary){
			AddPerson command = new AddPerson(personName);
			PersonAdded personAdded;
			try {
				personAdded = (PersonAdded) boundary.reactToMessage(command).get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return personAdded.getPersonId();
		}

		private void addCompany(String companyName, ContactListBoundary boundary) {
			AddCompany command = new AddCompany(companyName);
			reactToUserMessage(boundary, command);
		}

		private void renameContact(String contactId, String newName, ContactListBoundary boundary) {
			RenameContact command = new RenameContact(contactId, newName);
			reactToUserMessage(boundary, command);
		}

		private void reactToUserMessage(ContactListBoundary boundary, Object command) {
			try {
				boundary.reactToMessage(command).get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
