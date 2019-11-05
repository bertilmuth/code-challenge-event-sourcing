package contactlist.boundary.internal.command_handler;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import contactlist.boundary.internal.command_handler.HandleAddPerson;
import contactlist.boundary.internal.event.PersonAdded;
import contactlist.command.AddPerson;

public class HandleAddPersonTest {
	private static final String MAX_MUSTERMANN = "Max Mustermann";
	private static final String BERTIL_MUTH = "Bertil Muth";
	
	private HandleAddPerson commandHandler;
	
	@Before
	public void setup() {
		commandHandler = new HandleAddPerson();
	}
	
	@Test
	public void adds_person_to_contact_list() {
		AddPerson command = new AddPerson(MAX_MUSTERMANN);

		PersonAdded actualEvent = (PersonAdded)commandHandler.apply(command);
		assertEquals(MAX_MUSTERMANN, actualEvent.getPersonName());
	}
	
	@Test
	public void adds_different_person_to_contact_list() {
		AddPerson command = new AddPerson(BERTIL_MUTH);

		PersonAdded actualEvent = (PersonAdded)commandHandler.apply(command);
		assertEquals(BERTIL_MUTH, actualEvent.getPersonName());
	}
}
