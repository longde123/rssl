package net.allochie.vm.jass;

public class VMFunctionPointer {

	public final VMFunction functionVal;
	public final String functionName;

	public VMFunctionPointer(VMFunction what) {
		this.functionVal = what;
		this.functionName = null;
	}

	public VMFunctionPointer(String name) {
		this.functionVal = null;
		this.functionName = name;
	}

}
