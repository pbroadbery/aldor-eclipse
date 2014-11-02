package aldor.customnavigator.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import aldor.customnavigator.CustomProjectWorkbenchRoot;

public class AldorContentProvider implements ITreeContentProvider {

    private static final Object[] NO_CHILDREN = {};
    private CustomProjectParent[] _customProjectParents;
 
    @Override
    public Object[] getChildren(Object parentElement) {
        Object[] children = null;
        if (CustomProjectWorkbenchRoot.class.isInstance(parentElement)) {
            if (_customProjectParents == null) {
                _customProjectParents = initializeParent(parentElement);
            }
 
            children = _customProjectParents;
        } else {
            children = NO_CHILDREN;
        }
 
        return children;
    }
 
    private CustomProjectParent[] initializeParent(Object parentElement) {
        IProject [] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        CustomProjectParent[] result = new CustomProjectParent[projects.length];
        for (int i = 0; i < projects.length; i++) {
            result[i] = new CustomProjectParent(projects[i]);
        }
 
        return result;
    }
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return false;
	}

}
