package contactlist.boundary.internal.event;

import eventstore.Event;

public class PersonAdded extends Event{
	private String personId;
	private String personName;
	
	public PersonAdded(String personId, String personName) {
		this.personId = personId;
		this.personName = personName;
	}

	public String getPersonName() {
		return personName;
	}

	public String getPersonId() {
		return personId;
	}
}
