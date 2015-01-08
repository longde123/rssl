package net.allochie.vm.jass;

public class VMFunctionPointer {

	public final VMFunction functionVal;
	public final String functionName;

	public VMFunctionPointer(VMFunction what) {
		functionVal = what;
		functionName = null;
	}

	public VMFunctionPointer(String name) {
		functionVal = null;
		functionName = name;
	}

	@Override
	public String toString() {
		if (functionVal != null)
			return "FunctionPointer: real ref < " + functionVal + " >";
		else
			return "FunctionPointer: name ref < " + functionName + " >";
	}

}
