package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.dec.VarDec;

public class VMVariable {

	public final VarDec dec;
	public final VMClosure closure;
	public VMValue value;

	public VMVariable(VMClosure closure, VarDec dec) {
		this.closure = closure;
		this.dec = dec;
	}

	public void init() throws VMException {
		if (dec.init != null)
			value = dec.init.resolve(closure);
	}

}
