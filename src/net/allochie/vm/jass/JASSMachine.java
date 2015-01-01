package net.allochie.vm.jass;

import java.util.HashMap;

import net.allochie.vm.jass.ast.Function;
import net.allochie.vm.jass.ast.Identifier;
import net.allochie.vm.jass.ast.JASSFile;
import net.allochie.vm.jass.ast.dec.Dec;
import net.allochie.vm.jass.ast.dec.GlobalsDec;
import net.allochie.vm.jass.ast.dec.NativeFuncDef;
import net.allochie.vm.jass.ast.dec.TypeDec;
import net.allochie.vm.jass.ast.dec.VarDec;

public class JASSMachine {

	public HashMap<String, TypeDec> types = new HashMap<String, TypeDec>();

	public HashMap<String, VMVariable> globals = new HashMap<String, VMVariable>();

	public HashMap<String, NativeFuncDef> natives = new HashMap<String, NativeFuncDef>();
	public HashMap<String, Function> funcs = new HashMap<String, Function>();

	public void doFile(VMClosure closure, JASSFile file) throws VMException {
		for (Dec what : file.decs) {
			if (what instanceof TypeDec) {
				TypeDec type = (TypeDec) what;
				if (type.type == null)
					if (!types.containsKey(type.typename.image))
						throw new VMException("Cannot extend unknown type " + type.typename.image);
				types.put(type.id.image, type);
			} else if (what instanceof GlobalsDec) {
				GlobalsDec heap = (GlobalsDec) what;
				for (VarDec var : heap.decs) {
					if (globals.containsKey(var.name.image) && globals.get(var.name.image).dec.constant)
						throw new VMException("Cannot redeclare existing variable " + var.name.image);
					globals.put(var.name.image, new VMVariable(closure, var));
					globals.get(var.name.image).init();
				}
			} else if (what instanceof NativeFuncDef) {
				NativeFuncDef nativeFn = (NativeFuncDef) what;
				natives.put(nativeFn.def.id.image, nativeFn);
			} else
				throw new VMException("Unknown definition type " + what.getClass().getName());
		}

		for (Function func : file.funcs)
			funcs.put(func.sig.id.image, func);

	}

	public VMVariable findGlobal(Identifier identifier) {
		// TODO Auto-generated method stub
		return null;
	}

}
