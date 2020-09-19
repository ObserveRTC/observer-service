package org.observertc.webrtc.observer;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * https://stackabuse.com/example-loading-a-java-class-at-runtime/
 *
 * <p>
 * ExtensionLoader<MyPlugin> loader = new ExtensionLoader<MyPlugin>();
 * somePlugin = loader.LoadClass("path/to/jar/file", "com.example.pluginXYZ", MyPlugin.class);
 * </p>
 *
 * @param <C>
 */
public class ExtensionLoader<C> {

	public C LoadClass(String directory, String classpath, Class<C> parentClass) throws ClassNotFoundException {
		File pluginsDir = new File(System.getProperty("user.dir") + directory);
		for (File jar : pluginsDir.listFiles()) {
			try {
				ClassLoader loader = URLClassLoader.newInstance(
						new URL[]{jar.toURL()},
						getClass().getClassLoader()
				);
				Class<?> clazz = Class.forName(classpath, true, loader);
				Class<? extends C> newClass = clazz.asSubclass(parentClass);
				// Apparently its bad to use Class.newInstance, so we use 
				// newClass.getConstructor() instead
				Constructor<? extends C> constructor = newClass.getConstructor();
				return constructor.newInstance();

			} catch (ClassNotFoundException e) {
				// There might be multiple JARs in the directory,
				// so keep looking
				continue;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
		throw new ClassNotFoundException("Class " + classpath
				+ " wasn't found in directory " + System.getProperty("user.dir") + directory);
	}
}
