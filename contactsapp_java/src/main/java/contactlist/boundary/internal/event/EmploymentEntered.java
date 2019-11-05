package contactlist.boundary.internal.event;

import eventstore.Event;

public class EmploymentEntered extends Event{
	private String personId;
	private String companyId;
	private String role;

	public EmploymentEntered(String personId, String companyId, String role) {
		this.personId = personId;
		this.companyId = companyId;
		this.role = role;
	}

	public String getPersonId() {
		return personId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public String getRole() {
		return role;
	}
}
