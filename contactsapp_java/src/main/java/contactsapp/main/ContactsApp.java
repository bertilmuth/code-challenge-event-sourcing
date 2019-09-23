package contactsapp.main;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
		EventStore eventStore = new EventStore();

		for (int i = 0; i < 100; i++) {
			final EventSender eventSender = new EventSender(eventStore);
			Thread eventSenderThread = new Thread(eventSender);
			eventSenderThread.start();
		}

	}

	private static class EventSender implements Runnable {
		private EventStore eventStore;

		public EventSender(EventStore eventStore) {
			this.eventStore = eventStore;
		}

		@Override
		public void run() {
			ContactListBoundary boundary = createBoundary();

			System.out.println("Adding person: " + PERSON_NAME);
			String personId = addPerson(PERSON_NAME, boundary);

			System.out.println("Adding company: " + COMPANY_NAME);
			addCompany(COMPANY_NAME, boundary);

			System.out.println("Renaming person: " + PERSON_NAME + " to: " + NEW_PERSON_NAME);
			renameContact(personId, NEW_PERSON_NAME, boundary);

			System.out.println("Replaying events...");
			ContactListBoundary newBoundary = createBoundary();
			// eventStore.replayWith(newBoundary);

			System.out.println("\nThe contacts are:");
			List<Contact> contacts = findContacts(newBoundary);
			//printToConsole(contacts);			
		}

		public ContactListBoundary createBoundary() {
			ContactListBoundary contactListBoundary = new ContactListBoundary(event ->  {});
			return contactListBoundary;
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
			boundary.reactToUserMessage(command);
		}

		private void renameContact(String contactId, String newName, ContactListBoundary boundary) {
			RenameContact command = new RenameContact(contactId, newName);
			boundary.reactToUserMessage(command);
		}

		@SuppressWarnings("unchecked")
		private List<Contact> findContacts(ContactListBoundary boundary) {
			FindContacts query = new FindContacts();
			try {
				return (List<Contact>)boundary.reactToUserMessage(query).get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		private void printToConsole(List<Contact> contacts) {
			for (Contact contact : contacts) {
				System.out.println(contact);
			}
		}

	}
}
