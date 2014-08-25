package aldor.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class AldorCore extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "aldor.core"; //$NON-NLS-1$

	// The shared instance
	private static AldorCore plugin;
	
	/**
	 * The constructor
	 */
	public AldorCore() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static AldorCore getDefault() {
		return plugin;
	}
	public static void log(String e) {
		log(createStatus(e));
	}

	public static void log(int severity, String msg) {
		log(new Status(severity, PLUGIN_ID, msg));
	}
	public static void logStackTrace(int severity, String msg) {
		log(new Status(severity, PLUGIN_ID, msg, new Exception()));
	}
	public static void log(Throwable e) {
		String msg= e.getMessage();
		if (msg == null) {
			log("Error", e); //$NON-NLS-1$
		} else {
			log("Error: " + msg, e); //$NON-NLS-1$
		}
	}

	public static void log(String message, Throwable e) {
		log(createStatus(message, e));
	}

	public static IStatus createStatus(String msg) {
		return createStatus(msg, null);
	}
	public static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

}
