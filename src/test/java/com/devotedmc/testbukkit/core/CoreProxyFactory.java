package com.devotedmc.testbukkit.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.devotedmc.testbukkit.ProxyFactory;
import com.devotedmc.testbukkit.ProxyMock;
import com.devotedmc.testbukkit.TestPlayer;
import com.devotedmc.testbukkit.annotation.ProxyStub;
import com.devotedmc.testbukkit.annotation.ProxyTarget;

import javassist.Modifier;

public class CoreProxyFactory implements ProxyFactory {
	
	private static final Map<Class<?>, Class<? extends ProxyMock<?>>> proxyMocks = new HashMap<Class<?>, Class<? extends ProxyMock<?>>>();
	
	static {
		registerProxyInternal(CoreTestPlayer.class);
		registerProxyInternal(CoreTestBlock.class);
		registerProxyInternal(CoreTestWorld.class);
		registerProxyInternal(CoreTestChunk.class);
		registerProxyInternal(CoreTestBlockState.class);
		registerProxyInternal(CoreTestInventory.class);
	}
	
	public CoreProxyFactory() {
	}

	@Override
	public TestPlayer createPlayer(String name, UUID uid) {
		return createInstance(TestPlayer.class, name, uid);
	}
	
	@Override
	public TestPlayer createPlayer(String name) {
		return createPlayer(name, UUID.randomUUID());
	}

	@Override
	public <T> T createInstance(Class<T> clazz, Object... initArgs) {
		try {
			Class<? extends ProxyMock<?>> proxyType = proxyMocks.get(clazz);
			if (proxyType != null) {
				Constructor<?> c = proxyType.getDeclaredConstructors()[0];
				return clazz.cast(ProxyMock.class.cast(c.newInstance(initArgs)).getProxy());
			}
		} catch (Exception ex) {
			throw new RuntimeException("Unable to create proxy for " + clazz.getName());
		}
		return null;
	}

	@Override
	public void registerProxy(Class<? extends ProxyMock<?>> proxyType) {
		registerProxyInternal(proxyType);
	}
	
	private static void registerProxyInternal(Class<? extends ProxyMock<?>> proxyType) {
		ProxyTarget targetAnnotation = proxyType.getAnnotation(ProxyTarget.class);
		if (targetAnnotation == null) {
			throw new Error(String.format("Proxy class %s must be annotated with %s", proxyType.getName(), ProxyTarget.class.getName()));
		}
		Class<?> targetType = targetAnnotation.value();
		
		// Make sure any stubbed methods actually exist in the target class
		for (Method m : proxyType.getDeclaredMethods()) {
			ProxyStub stub = m.getAnnotation(ProxyStub.class);
			if (stub == null) {
				continue;
			}			
			boolean match = false;
			for (Method m2 : targetType.getMethods()) {
				if (m2.getName().equals(m.getName()) && Arrays.equals(m.getParameterTypes(), m2.getParameterTypes()) && m.getReturnType().equals(m2.getReturnType())) {
					
					if (!Modifier.isPublic(m2.getModifiers())) {
						throw new NoSuchMethodError(String.format("The proxy stub %s.%s() isn't public", proxyType.getSimpleName(), m.getName()));
					}
					
					match = true;
					break;
				}
			}
			
			if (!match) {
				String params = "";
				Class<?>[] paramTypes = m.getParameterTypes();
				if (paramTypes.length == 0) {
					params = "void";
				} else {
					for (Class<?> p : paramTypes) {
						params += p.getSimpleName();
						params += ", ";
					}
					params = params.substring(0, params.length() - 2);
				}
				
				
				throw new NoSuchMethodError(String.format("The proxy stub %s.%s(%s) doesn't exist in %s", proxyType.getSimpleName(), m.getName(), params, targetType.getName()));
			}
		}
		
		proxyMocks.put(targetType, proxyType);
	}
}