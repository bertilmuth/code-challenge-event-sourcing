package contactlist;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import contactlist.boundary.ContactListBoundaryTest;
import contactlist.boundary.internal.command_handler.HandleAddCompanyTest;
import contactlist.boundary.internal.command_handler.HandleAddPersonTest;
import contactlist.boundary.internal.command_handler.HandleEnterEmploymentTest;
import contactlist.boundary.internal.command_handler.HandleRenameContactTest;
import contactlist.boundary.internal.event_handler.HandleCompanyAddedTest;
import contactlist.boundary.internal.event_handler.HandleContactRenamedTest;
import contactlist.boundary.internal.event_handler.HandleEmploymentEnteredTest;
import contactlist.boundary.internal.event_handler.HandlePersonAddedTest;

@RunWith(Suite.class)
@SuiteClasses({ HandleAddCompanyTest.class, HandleAddPersonTest.class, HandleRenameContactTest.class,
		HandleEnterEmploymentTest.class, HandleCompanyAddedTest.class, HandlePersonAddedTest.class,
		HandleContactRenamedTest.class, HandleEmploymentEnteredTest.class, ContactListBoundaryTest.class })
public class AllTests {
}
