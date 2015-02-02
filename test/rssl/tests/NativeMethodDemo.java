package rssl.tests;

import net.allochie.vm.rssl.runtime.api.Callout;
import net.allochie.vm.rssl.runtime.api.NativeMethod;
import net.allochie.vm.rssl.runtime.value.VMType;

public class NativeMethodDemo {

	@NativeMethod(name = "PrintConsole")
	public void printString(Callout call, String what) {
		System.out.println(what);
	}

	@NativeMethod(name = "What")
	public String getTypeof(Callout call, Object what) {
		return VMType.findType(call.machine, what).typename;
	}

}
