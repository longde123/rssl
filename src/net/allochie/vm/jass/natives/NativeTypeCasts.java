package net.allochie.vm.jass.natives;

import net.allochie.vm.jass.global.Callout;
import net.allochie.vm.jass.global.NativeMethod;

public class NativeTypeCasts {

	@NativeMethod(name = "I2S")
	public static String I2S(Callout c, Integer i) {
		return Integer.toString(i);
	}
}
