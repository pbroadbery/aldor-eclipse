package aldor.project.wizard.aldorsource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class WizardNewAldorSourceFileCreationPage extends WizardNewFileCreationPage {

	public WizardNewAldorSourceFileCreationPage(IStructuredSelection selection) {
        super("Aldor New Source File Wizard", selection);

        setTitle("Schema File Wizard");
        setDescription("Create an Aldor Source File");
        setFileExtension("as");
    }

    @Override
    protected InputStream getInitialContents() {
        String content = "#include \"incl.as\"\n"; // FIXME - should check project default (if any)
        return new ByteArrayInputStream(content.getBytes());
    }
}
