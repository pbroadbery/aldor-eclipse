package aldor.project.wizard.aldorsource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import aldor.core.project.AldorPreferenceModel;
import aldor.project.builder.AldorNature;
import aldor.project.builder.AldorProjectSupport;
import aldor.utils.ui.IEditorParts;

public class WizardNewAldorSourceFileCreationPage extends WizardNewFileCreationPage {

    static final private AldorPreferenceModel preferenceModel = AldorPreferenceModel.instance();

	private IProject selectedProject;

	public WizardNewAldorSourceFileCreationPage(IWorkbench workbench, IStructuredSelection selection) {
        super("Aldor New Source File Wizard", selection);

        setTitle("Schema File Wizard");
        IProject selectedProject = selectedProject(workbench, selection);
		setDescription("Create an Aldor Source File " + (selectedProject == null ? "" : "(in " + selectedProject.getName() + ")"));
        setFileExtension("as");

        this.selectedProject = selectedProject;
    }

    private IProject selectedProject(IWorkbench workbench, IStructuredSelection selection) {
    	Iterator<?> iterator = selection.iterator();
    	IProject theProject = null;
    	while (iterator.hasNext()) {
    		Object o = iterator.next();
    		if (o instanceof IResource) {
    			IResource res = (IResource) o;
    			theProject = res.getProject();
    		}
    	}

    	if (theProject == null) {
    		IWorkbenchPart part = workbench.getActiveWorkbenchWindow().getActivePage().getActivePart();
    		if (part instanceof IEditorPart) {
    			IEditorPart textEditor = (IEditorPart) part;
    			IResource resource = IEditorParts.extractResource(textEditor);
    			theProject = resource.getProject();
    		}
    	}

    	return theProject;
    }


	@Override
    protected InputStream getInitialContents() {
		AldorNature nature;
		try {
			nature = selectedProject == null ? null : AldorProjectSupport.aldorNature(selectedProject);
			if (nature != null) {
				IPreferenceStore preferenceStore = AldorProjectSupport.getPreferenceStore(selectedProject);
				String includeFileName = preferenceModel.includeFileName.preference(preferenceStore);
		        if (includeFileName != null) {
		        	String content = "#include \"" + includeFileName + "\"\n";
			        return new ByteArrayInputStream(content.getBytes());
		        }
			}
		    return new ByteArrayInputStream(new byte[0]);
		} catch (CoreException e) {
			return new ByteArrayInputStream(new byte[0]);
		}
	}
}
