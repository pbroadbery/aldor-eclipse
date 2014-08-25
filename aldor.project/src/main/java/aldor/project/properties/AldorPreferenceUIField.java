package aldor.project.properties;

import aldor.core.project.AldorPreferenceModel.AldorPreference;

public class AldorPreferenceUIField<T> {
	public AldorPreference<T> item;
	public String title;
	public T defaultValue;
	
	AldorPreferenceUIField(AldorPreference<T> item, T defaultValue, String title) {
		this.item = item;
		this.title = title;
		this.defaultValue = defaultValue;
	}

	public String defaultStringValue() {
		return item.encode(defaultValue);
	}

	public String title() {
		return title;
	}

	public T defaultValue() {
		return defaultValue;
	}

	public String name() {
		return item.name();
	}
	
	
}
