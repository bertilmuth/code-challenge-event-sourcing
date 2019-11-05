package contactlist.boundary.internal.command_handler;

import java.util.function.Function;

import contactlist.boundary.internal.event.CompanyAdded;
import contactlist.command.AddCompany;

public class HandleAddCompany implements Function<AddCompany, Object> {
	@Override
	public Object apply(AddCompany command) {
		String companyId = IdGenerator.newId();
		CompanyAdded event = new CompanyAdded(companyId, command.getCompanyName());
		return event;
	}
}
