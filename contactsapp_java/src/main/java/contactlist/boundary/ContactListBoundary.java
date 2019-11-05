package contactlist.boundary;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.requirementsascode.EventQueue;
import org.requirementsascode.Model;
import org.requirementsascode.ModelRunner;

import contactlist.boundary.internal.command_handler.HandleAddCompany;
import contactlist.boundary.internal.command_handler.HandleAddPerson;
import contactlist.boundary.internal.command_handler.HandleEnterEmployment;
import contactlist.boundary.internal.command_handler.HandleRenameContact;
import contactlist.boundary.internal.domain.ContactList;
import contactlist.boundary.internal.event.CompanyAdded;
import contactlist.boundary.internal.event.ContactRenamed;
import contactlist.boundary.internal.event.EmploymentEntered;
import contactlist.boundary.internal.event.PersonAdded;
import contactlist.boundary.internal.event_handler.HandleCompanyAdded;
import contactlist.boundary.internal.event_handler.HandleContactRenamed;
import contactlist.boundary.internal.event_handler.HandleEmploymentEntered;
import contactlist.boundary.internal.event_handler.HandlePersonAdded;
import contactlist.boundary.internal.query_handler.HandleFindContacts;
import contactlist.boundary.internal.validation.ValidationError;
import contactlist.command.AddCompany;
import contactlist.command.AddPerson;
import contactlist.command.EnterEmployment;
import contactlist.command.RenameContact;
import contactlist.query.FindContacts;
import eventstore.Event;

/**
 * The boundary class is the only point of communication with the outside world.
 * It accepts commands and queries, and calls the appropriate handlers.
 * 
 * The handler transforms each message into an event. The events are
 * handled by the event consumer specified as constructor argument,
 * typically an event store.
 * 
 * @author b_muth
 *
 */
public class ContactListBoundary {
	private Consumer<Object> eventConsumer;
	private ContactList contactList;
	private EventQueue futureMessageQueue;
	
	private ModelRunner messageModelRunner;
	private ModelRunner eventHandlingModelRunner;

	public ContactListBoundary() {
		this(event->{});
	}
	
	public ContactListBoundary(Consumer<Object> eventConsumer) {
		this.eventConsumer = eventConsumer;
		this.contactList = new ContactList();
		this.futureMessageQueue = new EventQueue(this::processFutureMessage);

		this.messageModelRunner = new ModelRunner().run(messageModel());
		this.eventHandlingModelRunner = new ModelRunner().run(eventModel());
	}

	private void processFutureMessage(Object futureMessageObject) {
		FutureMessage futureMessage = (FutureMessage) futureMessageObject;

		CompletableFuture<Object> future = futureMessage.getFuture();
		Object message = futureMessage.getMessage();

		messageModelRunner
			.publishWith(eventObject -> processEventObject(future, eventObject))
			.reactTo(message);
	}

	private void processEventObject(CompletableFuture<Object> future, Object eventObject) {
		if (eventObject instanceof Event) {
			Event event = (Event) eventObject;
			eventConsumer.accept(event);
			reactToEvent(event);
		}
		future.complete(eventObject);
	}
	

	/**
	 * Reacts to the specified user message (i.e. command or query) by sending it to
	 * its message handler, if there is one, and returning the result.
	 * 
	 * @param message the command or querye.
	 * @return a completable future with the published event, if the specified
	 *         message is a command, or the query result.
	 */
	public CompletableFuture<Object> reactToMessage(Object message) {
		CompletableFuture<Object> future = new CompletableFuture<>();
		FutureMessage futureMessage = new FutureMessage(future, message);
		futureMessageQueue.put(futureMessage);
		return future;
	}

	public void reactToEvent(Object eventObject) {
		eventHandlingModelRunner.reactTo(eventObject);
	}

	private Model messageModel() {
		Model model = Model.builder()
			.user(AddPerson.class).systemPublish(new HandleAddPerson())
			.user(AddCompany.class).systemPublish(new HandleAddCompany())
			.user(RenameContact.class).systemPublish(new HandleRenameContact(contactList))
			.user(EnterEmployment.class).systemPublish(new HandleEnterEmployment(contactList))
			.user(FindContacts.class).systemPublish(new HandleFindContacts(contactList))
			.on(ValidationError.class).systemPublish(err -> err) // Return validation errors

		.build();

		return model;
	}
	
	private Model eventModel() {
		Model model = Model.builder()
			.on(PersonAdded.class).system(new HandlePersonAdded(contactList))
			.on(CompanyAdded.class).system(new HandleCompanyAdded(contactList))
			.on(ContactRenamed.class).system(new HandleContactRenamed(contactList))
			.on(EmploymentEntered.class).system(new HandleEmploymentEntered(contactList))
		.build();

		return model;
	}

	private class FutureMessage {
		private final CompletableFuture<Object> future;
		private final Object message;

		public FutureMessage(CompletableFuture<Object> future, Object message) {
			this.future = future;
			this.message = message;
		}
		
		public CompletableFuture<Object> getFuture() {
			return future;
		}

		public Object getMessage() {
			return message;
		}
	}

	public void stop() {
		futureMessageQueue.stop();
	}
}