package aldor.customnavigator.navigator;

import org.eclipse.jdt.ui.ProblemsLabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class AldorLabelProvider extends LabelProvider implements ILabelProvider {
	ProblemsLabelDecorator decorator = new ProblemsLabelDecorator();

    @Override
    public String getText(Object element) {
        String text = "";
        if (CustomProjectParent.class.isInstance(element)) {
            text = ((CustomProjectParent)element).getProjectName();
        }

        return text;
    }


    @Override
	public Image getImage(Object element) {
        Image image = null;

        if (CustomProjectParent.class.isInstance(element)) {
            image = ((CustomProjectParent)element).getImage();
        }
        // else ignore the element

        decorator.decorateImage(image, element);

        return image;
    }
}
