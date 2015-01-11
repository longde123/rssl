package net.allochie.vm.rssl.natives;

import net.allochie.vm.rssl.global.Callout;
import net.allochie.vm.rssl.global.NativeMethod;

public class NativeTypeCasts {

	@NativeMethod(name = "I2S")
	public static String I2S(Callout c, Integer i) {
		return Integer.toString(i);
	}
}
