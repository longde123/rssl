package net.allochie.vm.rssl.runtime.value;

import net.allochie.vm.rssl.ast.Function;

public class VMFunction extends Function {

	public VMFunction() {
	}

	public VMFunction(Function func) {
		constant = func.constant;
		lvars = func.lvars;
		sig = func.sig;
		statements = func.statements;
		where = func.where;
	}

}
