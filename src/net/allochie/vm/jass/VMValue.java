package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.Type;

public class VMValue {

	public Type type;
	public Object value;

	public VMValue() {
	}

	public VMValue(Object value, Type type) {
		this.type = type;
		this.value = value;
	}

}
