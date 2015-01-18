package net.allochie.vm.rssl.natives;

import net.allochie.vm.rssl.VMException;

public class NativeMethodException extends VMException {

	public NativeMethodException(String reason) {
		super(null, reason);
	}

}
