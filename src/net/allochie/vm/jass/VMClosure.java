package net.allochie.vm.jass;

import java.util.HashMap;

import net.allochie.vm.jass.ast.Identifier;

public class VMClosure {

	public final VMClosure parent;
	public final JASSMachine machine;
	public final HashMap<String, VMVariable> vars = new HashMap<String, VMVariable>();

	public VMClosure(JASSMachine machine) {
		this.parent = null;
		this.machine = machine;
	}

	public VMClosure(VMClosure closure) {
		this.parent = closure;
		this.machine = null;
	}

	public boolean top() {
		return parent == null;
	}

	public VMVariable getVariable(Identifier identifier) throws VMException {
		VMVariable var;
		var = vars.get(identifier.image);
		if (var != null)
			return var;
		if (parent != null) {
			var = parent.vars.get(var);
			if (var != null)
				return var;
			throw new VMException("Undefined identifier " + identifier.image);
		} else {
			var = machine.findGlobal(identifier);
			if (var != null)
				return var;
			throw new VMException("Undefined identifier " + identifier.image);
		}
	}
}
