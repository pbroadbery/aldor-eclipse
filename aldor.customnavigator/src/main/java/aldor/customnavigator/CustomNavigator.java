package aldor.customnavigator;

import org.eclipse.ui.navigator.CommonNavigator;

public class CustomNavigator extends CommonNavigator {

	   @Override
	    protected Object getInitialInput() {
	        return new CustomProjectWorkbenchRoot();
	    }
	   
}
