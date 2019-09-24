package contactsapp.boundary.internal.command_handler;

import java.util.function.Function;

import contactsapp.boundary.internal.event.CompanyAdded;
import contactsapp.command.AddCompany;

public class HandleAddCompany implements Function<AddCompany, Object> {
	@Override
	public Object apply(AddCompany command) {
		String companyId = IdGenerator.newId();
		CompanyAdded event = new CompanyAdded(companyId, command.getCompanyName());
		return event;
	}
}
