package net.allochie.vm.rssl.runtime.api.natives;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import net.allochie.vm.rssl.runtime.VMException;

public class NativeMethodRegistry {

	public static ArrayList<Class<? extends Object>> map = new ArrayList<Class<? extends Object>>();
	public static HashMap<Class<? extends Object>, Object> vals = new HashMap<Class<? extends Object>, Object>();

	public static Object findAndInvokeNative(String image, Class<?>[] args, Object[] params) throws VMException {
		Method method = null;
		Object owner = null;
		for (Class<? extends Object> zz : map) {
			if (!vals.containsKey(zz))
				try {
					vals.put(zz, zz.newInstance());
				} catch (Throwable t) {
					throw new VMException(zz, "Can't instantiate native method provider " + zz.getName());
				}
			for (Method m : zz.getMethods())
				if (m.isAnnotationPresent(NativeMethod.class)) {
					NativeMethod z0 = m.getAnnotation(NativeMethod.class);
					if (z0.name().equalsIgnoreCase(image)) {
						Class<?>[] fargs = m.getParameterTypes();
						if (fargs.length == args.length) {
							boolean flag = true;
							for (int i = 0; i < fargs.length; i++)
								if (fargs[i] != Object.class)
									if (fargs[i] != args[i])
										flag = false;
							if (flag) {
								owner = vals.get(zz);
								method = m;
							}
						}
					}
				}
		}
		if (method == null) {
			StringBuilder typename = new StringBuilder();
			typename.append(image).append(" (");
			for (int i = 0; i < args.length; i++) {
				typename.append(args[i].getName());
				if (i < args.length - 1)
					typename.append(", ");
			}
			typename.append(")");
			throw new VMException(image, "Unresolved native method " + typename.toString());
		}
		try {
			return method.invoke(owner, params);
		} catch (Throwable t) {
			throw new VMException(method, "Error invoking method", t);
		}
	}

	public static void registerNativeMethodProvider(Class<? extends Object> what) {
		if (!map.contains(what))
			map.add(what);
	}
}
