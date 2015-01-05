package net.allochie.vm.jass.tests;

import net.allochie.vm.jass.VMType;
import net.allochie.vm.jass.global.Callout;
import net.allochie.vm.jass.global.NativeMethod;

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
