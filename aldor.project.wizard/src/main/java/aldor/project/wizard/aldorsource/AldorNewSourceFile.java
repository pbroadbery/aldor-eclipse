package aldor.project.wizard.aldorsource;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class AldorNewSourceFile extends Wizard implements INewWizard {
	private WizardNewFileCreationPage _pageOne;

	private IWorkbench _workbench;
	private IStructuredSelection _selection;
	public AldorNewSourceFile() {
		setWindowTitle("New Aldor Source File");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	    _workbench = workbench;
	    _selection = selection;
	}

	@Override
	public boolean performFinish() {
	    boolean result = false;

	    IFile file = _pageOne.createNewFile();
	    result = file != null;

	    if (result) {
	        try {
	            IEditorPart editor = IDE.openEditor(_workbench.getActiveWorkbenchWindow().getActivePage(), file);
	            if (editor instanceof ITextEditor) {
	            	ITextEditor textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
	            	IDocumentProvider provider = textEditor.getDocumentProvider();
	            	IDocument document = provider.getDocument(editor.getEditorInput());
	            	textEditor.selectAndReveal(document.getLength(), 0);
	            }
	        } catch (PartInitException e) {
	            e.printStackTrace();
	        }
	    } // else no file created...result == false

	    return result;
	}

	@Override
	public void addPages() {
	    super.addPages();

	    _pageOne = new WizardNewAldorSourceFileCreationPage(_selection);

	    addPage(_pageOne);
	}
}
