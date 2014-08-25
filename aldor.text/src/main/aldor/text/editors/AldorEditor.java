package aldor.text.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class AldorEditor extends TextEditor {

	private ColorManager colorManager;

	public AldorEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new XMLConfiguration(colorManager));
		setDocumentProvider(new XMLDocumentProvider());
	}
	@Override
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

}
