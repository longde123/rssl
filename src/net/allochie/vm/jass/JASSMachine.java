package net.allochie.vm.jass;

import java.util.HashMap;

import net.allochie.vm.jass.ast.JASSFile;
import net.allochie.vm.jass.ast.dec.Dec;
import net.allochie.vm.jass.ast.dec.TypeDec;

public class JASSMachine {

	public final HashMap<String, VMType> types = new HashMap<String, VMType>();

	public void doFile(VMClosure closure, JASSFile file) throws VMException {
		for (Dec what : file.decs) {
			if (what instanceof TypeDec) {
				TypeDec type = (TypeDec) what;
				String typename = type.id.image;
				if (type.type != null)
					types.put(typename, new VMType(typename, type.type));
				else {
					String inferred = type.typename.image;
					if (!types.containsKey(inferred))
						throw new VMException("Cannot extend unknown type " + inferred);
					types.put(typename, new VMType(typename, inferred));
				}
			}
		}
	}

}
