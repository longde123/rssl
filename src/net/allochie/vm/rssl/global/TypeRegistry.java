package net.allochie.vm.rssl.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.allochie.vm.rssl.RSSLMachine;
import net.allochie.vm.rssl.VMType;
import net.allochie.vm.rssl.ast.Type;

public class TypeRegistry {
	public static HashMap<String, VMType> typeMap = new HashMap<String, VMType>();
	public static HashMap<String, ArrayList<Class<? extends Object>>> dict = new HashMap<String, ArrayList<Class<? extends Object>>>();

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

	public static void registerTypeWithClass(String typename, Class<? extends Object> what) {
		if (!dict.containsKey(typename.toLowerCase()))
			dict.put(typename.toLowerCase(), new ArrayList<Class<? extends Object>>());
		dict.get(typename.toLowerCase()).add(what);
	}

	public static Type fromString(String type) {
		if (!TypeRegistry.typeMap.containsKey(type))
			TypeRegistry.typeMap.put(type, new VMType(type));
		return TypeRegistry.typeMap.get(type);
	}

}
