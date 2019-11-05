package contactlist.boundary.internal.event_handler;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import contactlist.boundary.internal.domain.Company;
import contactlist.boundary.internal.domain.ContactList;
import contactlist.boundary.internal.domain.Person;
import contactlist.boundary.internal.event.ContactRenamed;
import contactlist.boundary.internal.event_handler.HandleContactRenamed;

public class HandleContactRenamedTest {
	private static final String BERTIL_MUTH = "Bertil Muth";
	private static final String MAX_MUSTERMANN = "Max Mustermann";
	private static final String FOO_COM = "Foo.com";
	private static final String BAR_COM = "Bar.com";

	private ContactList contactList;
	private HandleContactRenamed eventHandler;

	@Before
	public void setup() {
		contactList = new ContactList();
		eventHandler = new HandleContactRenamed(contactList);
	}

	@Test
	public void renames_person() {
		contactList.addPerson(BERTIL_MUTH, BERTIL_MUTH);

		ContactRenamed contactRenamed = new ContactRenamed(BERTIL_MUTH, MAX_MUSTERMANN);
		eventHandler.accept(contactRenamed);

		Person person = (Person) contactList.getContact(BERTIL_MUTH).get();
		assertEquals(MAX_MUSTERMANN, person.getName());
	}

	@Test
	public void renames_company() {
		contactList.addCompany(FOO_COM, FOO_COM);

		ContactRenamed contactRenamed = new ContactRenamed(FOO_COM, BAR_COM);
		eventHandler.accept(contactRenamed);

		Company company = (Company) contactList.getContact(FOO_COM).get();
		assertEquals(BAR_COM, company.getName());
	}
}
