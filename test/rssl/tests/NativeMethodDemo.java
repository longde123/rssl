package rssl.tests;

import net.allochie.vm.rssl.runtime.api.natives.Callout;
import net.allochie.vm.rssl.runtime.api.natives.NativeMethod;

public class NativeMethodDemo {

	@NativeMethod(name = "PrintConsole")
	public void printString(Callout call, String what) {
		System.out.println(what);
	}

}
