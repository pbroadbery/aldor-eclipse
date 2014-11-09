package aldor.utils.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

public class IEditorParts {
    static public IResource extractResource(IEditorPart editor) {
        IEditorInput input = editor.getEditorInput();
        if (!(input instanceof IFileEditorInput))
           return null;
        return ((IFileEditorInput)input).getFile();
     }
}
