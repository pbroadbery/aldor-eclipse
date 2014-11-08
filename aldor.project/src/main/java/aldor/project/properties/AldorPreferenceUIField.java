package aldor.project.properties;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

import com.google.common.base.Function;

import aldor.core.project.AldorPreferenceModel.AldorPreference;
import aldor.util.SafeCallable;

public class AldorPreferenceUIField<T> {
	private final AldorPreference<T> item;
	private final String title;
	private final SafeCallable<T> defaultValue;
	private Function<Composite, FieldEditor> fieldEditor;
	
	AldorPreferenceUIField(AldorPreference<T> item, String title, SafeCallable<T> defaultValue) {
		this.item = item;
		this.title = title;
		this.defaultValue = defaultValue;
	}
	
	AldorPreferenceUIField(final AldorPreference<T> item, final String title, final T defaultValue) {
		this(item,  title, new SafeCallable<T>() {
			@Override
			public T call() {
				return defaultValue;
			}
		});
	}

	public void fieldEditor(Function<Composite, FieldEditor> fieldEditor) {
		this.fieldEditor = fieldEditor;
	}
	
	public String defaultStringValue() {
		return item.encode(defaultValue.call());
	}

	public FieldEditor fieldEditor(Composite parent) {
		return fieldEditor.apply(parent);
	}
	
	public String title() {
		return title;
	}

	public T defaultValue() {
		return defaultValue.call();
	}

	public String name() {
		return item.name();
	}

	public String tooltip() {
		return null;
	}

	public AldorPreference<T> item() {
		return item;
	}
	
	public interface FieldEditorBuilder {}
	
}
