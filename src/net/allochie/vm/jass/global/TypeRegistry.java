package net.allochie.vm.jass.global;

import net.allochie.vm.jass.JASSMachine;
import net.allochie.vm.jass.ast.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class TypeRegistry {

	public static HashMap<String, ArrayList<Class<? extends Object>>> dict = new HashMap<String, ArrayList<Class<? extends Object>>>();

	public static Type findPreferredType(Object z, JASSMachine machine) {
		Type t0 = null;
		main: for (Entry<String, ArrayList<Class<? extends Object>>> entry : dict.entrySet()) {
			for (Class<? extends Object> what : entry.getValue())
				if (z.getClass().equals(what)) {
					t0 = machine.types.get(entry.getKey());
					break main;
				}
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

}
