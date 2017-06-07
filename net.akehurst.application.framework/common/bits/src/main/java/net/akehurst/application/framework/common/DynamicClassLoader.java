package net.akehurst.application.framework.common;

public class DynamicClassLoader extends ClassLoader {

	public DynamicClassLoader(final ClassLoader parent) {
		super(parent);
	}

	public synchronized Class<?> defineClass(final String fullClassName, final byte[] bytes) {
		try {
			return this.defineClass(fullClassName, bytes, 0, bytes.length);
		} catch (final ClassFormatError e) {
			// TODO: should log!
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {
		return super.loadClass(name, true);
	}

	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return super.findClass(name);
	}
}
