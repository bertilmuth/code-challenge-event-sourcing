package contactlist.boundary.internal.command_handler;

import java.util.function.Function;

import contactlist.boundary.internal.event.PersonAdded;
import contactlist.command.AddPerson;

public class HandleAddPerson implements Function<AddPerson, Object> {
	@Override
	public Object apply(AddPerson command) {
		String companyId = IdGenerator.newId();
		PersonAdded event = new PersonAdded(companyId, command.getPersonName());
		return event;
	}
}
