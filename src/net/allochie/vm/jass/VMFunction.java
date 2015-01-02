package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.Function;

public class VMFunction extends Function {

	public VMFunction(Function func) {
		this.constant = func.constant;
		this.lvars = func.lvars;
		this.sig = func.sig;
		this.statements = func.statements;
	}

	public VMValue runFunction(VMValue[] params) throws VMException {
		int nparams = params.length;
		if (nparams != sig.params.size())
			throw new VMException("Incorrect number of parameters for function call");
		for (int i = 0; i < nparams; i++)
			if (sig.params.get(i).type != params[i].type)
				throw new VMException("Incorrect parameter type for function call");
		// TODO: call function on stack (?)
		return new VMValue(null);
	}

}
