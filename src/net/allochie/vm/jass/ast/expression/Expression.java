package net.allochie.vm.jass.ast.expression;

import net.allochie.vm.jass.VMClosure;
import net.allochie.vm.jass.VMException;
import net.allochie.vm.jass.VMValue;

public abstract class Expression {

	public abstract VMValue resolve(VMClosure closure) throws VMException;

}
