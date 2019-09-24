package contactsapp.boundary.internal.command_handler;

import java.util.function.Function;

import contactsapp.boundary.internal.event.PersonAdded;
import contactsapp.command.AddPerson;

public class HandleAddPerson implements Function<AddPerson, Object> {
	@Override
	public Object apply(AddPerson command) {
		String companyId = IdGenerator.newId();
		PersonAdded event = new PersonAdded(companyId, command.getPersonName());
		return event;
	}
}
