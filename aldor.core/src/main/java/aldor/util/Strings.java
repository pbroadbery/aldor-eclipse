package aldor.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class Strings {

	private static final Strings instance = new Strings();
	private ConcurrentMap<Class<?>, StringCodec<?>> stringCodecForClass = new ConcurrentHashMap<Class<?>, StringCodec<?>>();
	public <S> StringCodec<S> stringCodec(Class<S> otherClss) {
		return null;
	}
	
	private Strings() {
		populate();
	}
	
	private void populate() {
		assert stringCodecForClass.isEmpty();
		
		stringCodecFor(String.class, new StringCodec<String>() {
			@Override
			public String decode(String to) {
				return to;
			}});
		stringCodecFor(Boolean.class, new StringCodec<Boolean>() {
			@Override
			public Boolean decode(String to) {
				return Boolean.valueOf(to);
			}});
		stringCodecFor(IPath.class, new StringCodec<IPath>() {
			@Override
			public IPath decode(String to) {
				return Path.fromPortableString(to);
			}});
	}

	@SuppressWarnings("unchecked")
	public <T> StringCodec<T> stringCodecFor(Class<T> clss) {
		if (!stringCodecForClass.containsKey(clss))
			throw new RuntimeException("Missing " + clss.getName());
		return (StringCodec<T>) stringCodecForClass.get(clss);
	}

	public <T> void stringCodecFor(Class<T> clss, StringCodec<T> codec) {
		stringCodecForClass.put(clss, codec);
	}
	
	public <S> S decode(Class<S> clss, String txt) {
		return txt == null ? null : stringCodecFor(clss).decode(txt);
	}
	
	public <S> String encode(Class<S> clss, S value) {
		return value == null ? null : stringCodecFor(clss).encode(value);
	}

	public interface Codec<From, To> {
		public To encode(From from);
		public From decode(To to);
	}
	
	abstract class StringCodec<T> implements Codec<T, String> {
		@Override
		public String encode(T value) {
			return value.toString();
		}
	}

	public static Strings instance() {
		return instance ;
	}
	
}
