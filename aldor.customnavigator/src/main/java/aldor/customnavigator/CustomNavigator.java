package aldor.customnavigator;

import org.eclipse.ui.navigator.CommonNavigator;

public class CustomNavigator extends CommonNavigator {

		CustomNavigator() {
		}

	   @Override
	    protected Object getInitialInput() {
	        return new CustomProjectWorkbenchRoot();
	    }

}
