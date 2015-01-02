package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.Function;

public class VMFunction extends Function {

	public VMFunction() {
	}

	public VMFunction(Function func) {
		this.constant = func.constant;
		this.lvars = func.lvars;
		this.sig = func.sig;
		this.statements = func.statements;
	}

}
