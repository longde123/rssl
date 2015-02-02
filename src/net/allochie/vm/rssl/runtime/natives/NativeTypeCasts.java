package net.allochie.vm.rssl.runtime.natives;

import net.allochie.vm.rssl.runtime.api.natives.Callout;
import net.allochie.vm.rssl.runtime.api.natives.NativeMethod;

public class NativeTypeCasts {

	@NativeMethod(name = "I2S")
	public static String I2S(Callout c, Integer i) {
		return Integer.toString(i);
	}
}
