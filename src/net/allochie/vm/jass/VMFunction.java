package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.Function;

public class VMFunction extends Function {

	public VMFunction() {
	}

	public VMFunction(Function func) {
		constant = func.constant;
		lvars = func.lvars;
		sig = func.sig;
		statements = func.statements;
	}

}
