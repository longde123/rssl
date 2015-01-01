package net.allochie.vm.jass;

import java.util.HashMap;

public class VMClosure {

	public final VMClosure parent;
	public final HashMap<String, VMVariable> vars = new HashMap<String, VMVariable>();

	public VMClosure() {
		this.parent = null;
	}

	public VMClosure(VMClosure closure) {
		this.parent = closure;
	}

	public boolean top() {
		return parent == null;
	}

}
