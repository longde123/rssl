package net.allochie.vm.rssl.ast;

public class ReturnType {

	public Type type;

	public boolean array;
	
	public ReturnType(Type type) {
		this(type, false);
	}
	
	public ReturnType(Type type, boolean array) {
		this.type = type;
		this.array = array;
	}

	@Override
	public String toString() {
		return type.toString() + ((array) ? "[]" : "");
	}

}
