package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.dec.NativeFuncDef;

public class VMNativeFunction extends VMFunction {

	public NativeFuncDef qd;

	public VMNativeFunction(NativeFuncDef qd) {
		super();
		this.qd = qd;
	}

	public VMValue executeNative(VMClosure closure) {
		
	}
	
	public void PrintInteger(int i) {
		System.out.println("PrintInteger: " + i);
	}

}
