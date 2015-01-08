package net.allochie.vm.jass;

import java.lang.reflect.Field;

import net.allochie.vm.jass.ast.CodePlace;

public class VMUserCodeException extends VMException {

	public VMUserCodeException(Object what, String reason) {
		super(what, reason);
	}

	public VMUserCodeException(Object what, String reason, Throwable t) {
		super(what, reason, t);
	}

}
