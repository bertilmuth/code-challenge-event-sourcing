package contactsapp.main;

import java.util.List;

import contactsapp.boundary.ContactListBoundary;
import contactsapp.boundary.internal.domain.Contact;
import contactsapp.boundary.internal.event.PersonAdded;
import contactsapp.command.AddCompany;
import contactsapp.command.AddPerson;
import contactsapp.command.RenameContact;
import contactsapp.query.FindContacts;
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

		for (int i = 0; i < 100; i++) {
			final EventSender eventSender = new EventSender(boundary);
			Thread eventSenderThread = new Thread(eventSender);
			eventSenderThread.start();
		}
		
		Thread.sleep(5000);
		
		System.out.println("Replaying events...");
		EventStore newEventStore = new EventStore();
		ContactListBoundary newBoundary = new ContactListBoundary(newEventStore);
		eventStore.replayWith(newBoundary::reactToEvent);

		System.out.println("\nThe contacts are:");
		List<Contact> contacts = findContacts(newBoundary);
		printToConsole(contacts);
		newBoundary.stopReacting();		
		
		boundary.stopReacting();
	}
	
	@SuppressWarnings("unchecked")
	private List<Contact> findContacts(ContactListBoundary boundary) {
		FindContacts query = new FindContacts();
		try {
			return (List<Contact>)boundary.reactToUserMessage(query).get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void printToConsole(List<Contact> contacts) {
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
			System.out.println("Adding person: " + PERSON_NAME);
			String personId = addPerson(PERSON_NAME, boundary);

			System.out.println("Adding company: " + COMPANY_NAME);
			addCompany(COMPANY_NAME, boundary);

			System.out.println("Renaming person: " + PERSON_NAME + " to: " + NEW_PERSON_NAME);
			renameContact(personId, NEW_PERSON_NAME, boundary);	
		}

		private String addPerson(String personName, ContactListBoundary boundary){
			AddPerson command = new AddPerson(personName);
			PersonAdded personAdded;
			try {
				personAdded = (PersonAdded) boundary.reactToUserMessage(command).get();
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
				boundary.reactToUserMessage(command).get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
