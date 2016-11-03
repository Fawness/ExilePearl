package com.devotedmc.testbukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.devotedmc.testbukkit.annotation.TestOptions;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

public class TestBukkitRunner extends BlockJUnit4ClassRunner {

	private static boolean modifiedJavaPlugin = false;

	public TestBukkitRunner(Class<?> clazz) throws InitializationError {
		super(clazz);

		boolean useLogger = false;
		boolean isIntegration = false;
		Class<? extends TestServer> serverInterface = TestServer.class;
		TestOptions testOptions = clazz.getDeclaredAnnotation(TestOptions.class);
		if (testOptions != null) {
			useLogger = testOptions.useLogger();
			isIntegration = testOptions.isIntegration();
			serverInterface = testOptions.server();
		}
		
		// Modify JavaPlugin if this is an integration test
		if (isIntegration) {
			rewireJavaPlugin();
		}

		TestServer server = TestBukkit.getServer();
		if (server == null) {
			try {
				ClassLoader loader = getClass().getClassLoader();
				Class<?> coreClass = loader.loadClass("com.devotedmc.testbukkit.core.CoreTestServer");
				if (!Arrays.asList(coreClass.getInterfaces()).contains(TestServer.class)) {
					throw new Exception();
				}

				Constructor<?> constructor = coreClass.getConstructor(boolean.class);
				constructor.setAccessible(true);
				server = (TestServer)constructor.newInstance(useLogger);
				
				if (serverInterface != TestServer.class) {
					TestServerProxy coreProxy = new TestServerProxy(server);
					
					server = (TestServer)Proxy.getProxyClass(loader, serverInterface).getConstructor(InvocationHandler.class).newInstance(coreProxy);
				}
			} catch (Exception ex) {
				throw new RuntimeException("Failed to create the TestServer instance", ex);
			}
		} else {
			server.configureLogger(useLogger);
		}
	}


	/**
	 * Modifies the code for JavaPlugin so that it can actually 
	 * be invoked outside of a Bukkit instance
	 * @throws InitializationError
	 */
	private void rewireJavaPlugin() throws InitializationError {
		if (modifiedJavaPlugin && !isJavaPluginLoaded()) {
			return;
		}

		try {
			ClassPool cp = new ClassPool(true);
			CtClass ctJavaPlugin = cp.get("org.bukkit.plugin.java.JavaPlugin");

			// Change the default constructor
			CtConstructor constructor = ctJavaPlugin.getConstructors()[0];
			constructor.setBody("{"
					+ "com.devotedmc.testbukkit.TestServer server = com.devotedmc.testbukkit.TestBukkit.getServer();"
					+ "this.server = server;"
					+ "this.loader = server.getPluginLoader();"
					+ "this.classLoader = getClass().getClassLoader();"
					+ "this.description = server.getDescription(this);"
					+ "this.dataFolder = server.getTestPluginDataFolder(this);"
					+ "this.file = server.getTestPluginFile(this);"
					+ "this.configFile = server.getTestPluginConfigFile(this);"
					+ "this.logger = new org.bukkit.plugin.PluginLogger(this);"
					+ "}");

			// Don't save anything to disk
			CtMethod ctSaveResource = ctJavaPlugin.getDeclaredMethod("saveResource");
			ctSaveResource.setBody("{ }");

			CtMethod ctSaveConfig = ctJavaPlugin.getDeclaredMethod("saveConfig");
			ctSaveConfig.setBody("{ }");

			CtMethod ctSaveDefaultConfig = ctJavaPlugin.getDeclaredMethod("saveDefaultConfig");
			ctSaveDefaultConfig.setBody("{ }");

			// Configuration gets pulled from the test server
			CtMethod ctReloadConfig = ctJavaPlugin.getDeclaredMethod("reloadConfig");
			ctReloadConfig.setBody("{ newConfig = com.devotedmc.testbukkit.TestBukkit.getServer().getPluginConfig(this); }");

			// Compile
			ctJavaPlugin.toClass();
			modifiedJavaPlugin = true;

		} catch (Exception ex) {
			throw new InitializationError("Failed to modify the JavaPlugin class.");
		}
	}

	private boolean isJavaPluginLoaded() throws InitializationError {
		try {
			// Method is protected, so make it public
			ClassLoader loader = getClass().getClassLoader();
			Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
			m.setAccessible(true);
			Object result = m.invoke(loader, "org.bukkit.plugin.java.JavaPlugin");
			return (result == null);
		} catch (Exception ex) {
			throw new InitializationError("Failed to check if JavaPlugin is loaded.");
		}
	}
}
