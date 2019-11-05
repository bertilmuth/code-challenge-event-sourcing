package contactlist.boundary.internal.event_handler;

import java.util.function.Consumer;

import contactlist.boundary.internal.domain.ContactList;
import contactlist.boundary.internal.event.CompanyAdded;

public class HandleCompanyAdded implements Consumer<CompanyAdded> {
	private ContactList contactList;

	public HandleCompanyAdded(ContactList contactList) {
		this.contactList = contactList;
	}
	
	@Override
	public void accept(CompanyAdded event) {
		String companyId = event.getCompanyId();
		String companyName = event.getCompanyName();
		contactList.addCompany(companyId, companyName);
	}
}
