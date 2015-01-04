package net.allochie.vm.jass.global;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import net.allochie.vm.jass.VMException;

public class NativeMethodRegistry {

	public static ArrayList<Class<? extends Object>> map = new ArrayList<Class<? extends Object>>();
	public static HashMap<Class<? extends Object>, Object> vals = new HashMap<Class<? extends Object>, Object>();

	public static Method findNativeMethod(String name) throws VMException {
		for (Class<? extends Object> zz : map) {
			if (!vals.containsKey(zz))
				try {
					vals.put(zz, zz.newInstance());
				} catch (Throwable t) {
					throw new VMException("Can't instantiate native method provider " + zz.getName());
				}
			for (Method m : zz.getMethods()) {
				if (m.isAnnotationPresent(NativeMethod.class)) {
					NativeMethod z0 = m.getAnnotation(NativeMethod.class);
					if (z0.name().equalsIgnoreCase(name))
						return m;
				}
			}
		}
		throw new VMException("Unresolved native method " + name);
	}

	public static void registerNativeMethodProvider(Class<? extends Object> what) {
		if (!map.contains(what))
			map.add(what);
	}
}
