package contactlist.boundary.internal.command_handler;

import java.util.Optional;
import java.util.function.Function;

import contactlist.boundary.internal.domain.Company;
import contactlist.boundary.internal.domain.Contact;
import contactlist.boundary.internal.domain.ContactList;
import contactlist.boundary.internal.domain.Person;
import contactlist.boundary.internal.event.EmploymentEntered;
import contactlist.boundary.internal.validation.MissingContact;
import contactlist.boundary.internal.validation.ShouldBeCompany;
import contactlist.boundary.internal.validation.ShouldBePerson;
import contactlist.command.EnterEmployment;

public class HandleEnterEmployment implements Function<EnterEmployment, Object>{
	private ContactList contactList;

	public HandleEnterEmployment(ContactList contactList) {
		this.contactList = contactList;
	}

	@Override
	public Object apply(EnterEmployment command) {
		String personId = command.getPersonId();
		String companyId = command.getCompanyId();
		String role = command.getRole();
		
		Optional<Contact> person = contactList.getContact(personId);
		Optional<Contact> company = contactList.getContact(companyId);

		if(!person.isPresent()) {
			return new MissingContact(personId);
		}
		if(!(person.get() instanceof Person)) {
			return new ShouldBePerson(personId);
		}
		if(!company.isPresent()) {
			return new MissingContact(companyId);
		}
		if(!(company.get() instanceof Company)) {
			return new ShouldBeCompany(companyId);
		}
		
		EmploymentEntered employmentEntered = new EmploymentEntered(personId, companyId,role);
		return employmentEntered;
	}

}
