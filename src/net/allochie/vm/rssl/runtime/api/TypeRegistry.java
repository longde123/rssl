package net.allochie.vm.rssl.runtime.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.runtime.RSSLMachine;
import net.allochie.vm.rssl.runtime.value.VMType;

public class TypeRegistry {
	/** The map of all typenames to Types */
	public static HashMap<String, VMType> typeMap = new HashMap<String, VMType>();
	/** The map of all typenames to Class types */
	public static HashMap<String, ArrayList<Class<? extends Object>>> dict = new HashMap<String, ArrayList<Class<? extends Object>>>();

	/**
	 * Find the preferred VM type for an object. If there is no preferred type
	 * found, HANDLE is returned.
	 * 
	 * @param z
	 *            The object instance
	 * @param machine
	 *            The RSSL current machine
	 * @return The object's formal type or HANDLE if no formal type (extending
	 *         HANDLE) exists to represent this object
	 */
	public static Type findPreferredType(Object z, RSSLMachine machine) {
		Type t0 = null;
		main: for (Entry<String, ArrayList<Class<? extends Object>>> entry : dict.entrySet())
			for (Class<? extends Object> what : entry.getValue())
				if (z.getClass().equals(what)) {
					t0 = machine.types.get(entry.getKey());
					break main;
				}
		if (t0 == null)
			return Type.handleType;
		return t0;
	}

	/**
	 * Bind a typename to a class object. A typename can represent multiple
	 * objects, but you should not register multiple typenames for one object.
	 * 
	 * @param typename
	 *            The name of the type
	 * @param what
	 *            The class of the type
	 */
	public static void registerTypeWithClass(String typename, Class<? extends Object> what) {
		if (!dict.containsKey(typename.toLowerCase()))
			dict.put(typename.toLowerCase(), new ArrayList<Class<? extends Object>>());
		dict.get(typename.toLowerCase()).add(what);
	}

	/**
	 * Derive a Type container for a String. Doesn't check if the type already
	 * exists or is valid; use with caution
	 * 
	 * @param type
	 *            The typename
	 * @return The Type container of the typename
	 */
	public static Type fromString(String type) {
		if (!TypeRegistry.typeMap.containsKey(type))
			TypeRegistry.typeMap.put(type, new VMType(type));
		return TypeRegistry.typeMap.get(type);
	}

}
