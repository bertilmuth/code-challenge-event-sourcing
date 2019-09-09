package contactsapp.boundary.internal.event;

import eventstore.Event;

public class CompanyAdded extends Event{
	private String companyId;
	private String companyName;

	public CompanyAdded(String companyId, String companyName) {
		this.companyId = companyId;
		this.companyName = companyName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getCompanyId() {
		return companyId;
	}
}
