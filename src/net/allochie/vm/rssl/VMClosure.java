package net.allochie.vm.rssl;

import java.util.HashMap;

import net.allochie.vm.rssl.ast.Identifier;
import net.allochie.vm.rssl.ast.dec.VarDec;

public class VMClosure {

	private final VMClosure parent;
	private final RSSLMachine machine;
	private final HashMap<String, VMVariable> vars = new HashMap<String, VMVariable>();

	public VMClosure(RSSLMachine machine) {
		parent = null;
		this.machine = machine;
	}

	public VMClosure(VMClosure closure) {
		parent = closure;
		machine = null;
	}

	public boolean top() {
		return parent == null;
	}

	public void createVariable(RSSLMachine machine, VarDec dec) throws VMException {
		machine.debugger.trace("closure.createVariable", this, dec);
		VMVariable var = new VMVariable(machine, this, dec);
		vars.put(var.dec.name.image, var);
	}

	public VMVariable getVariable(RSSLMachine machine, Identifier identifier) throws VMException {
		machine.debugger.trace("closure.getVariable", this, identifier);
		VMVariable var;
		var = vars.get(identifier.image);
		if (var != null)
			return var;
		if (parent != null) {
			var = parent.getVariable(machine, identifier);
			if (var != null)
				return var;
			throw new VMUserCodeException(identifier, "Undefined identifier " + identifier.image);
		} else {
			var = machine.findGlobal(identifier);
			if (var != null)
				return var;
			throw new VMUserCodeException(identifier, "Undefined identifier " + identifier.image);
		}
	}

	public VMVariable[] getAllVariables() {
		machine.debugger.trace("closure.getAllVariables", this);
		return vars.values().toArray(new VMVariable[0]);
	}
}
