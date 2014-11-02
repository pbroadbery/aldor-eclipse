package aldor.customnavigator.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;

import aldor.customnavigator.Activator;

public class CustomProjectParent {
 
    private IProject _project;
	private Image _image;

    public CustomProjectParent(IProject iProject) {
        _project = iProject;
    }
 
    public String getProjectName() {
        return _project.getName();
    }
    
    public Image getImage() {
        if (_image == null) {
            _image = Activator.getImage("icons/logo-folder-16.png"); //$NON-NLS-1$
        }
 
        return _image;
    }
    
    
}