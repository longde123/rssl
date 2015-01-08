package net.allochie.vm.jass;

import java.lang.reflect.Field;

import net.allochie.vm.jass.ast.CodePlace;

public class VMException extends Exception {
	
	public Object what;

	public VMException(Object what, String reason) {
		super(reason);
	}

	public VMException(Object what, String reason, Throwable t) {
		super(reason, t);
	}

	@Override
	public String toString() {
		StringBuilder what = new StringBuilder();
		what.append("Unrecoverable VM exception: ").append(getMessage()).append("\n");
		if (this.what != null) {
			try {
				Class<?> thing = this.what.getClass();
				for (Field f : thing.getFields()) {
					if (f.getType().equals(CodePlace.class)) {
						f.setAccessible(true);
						CodePlace place = (CodePlace) f.get(this.what);
						if (place != null) {
							what.append("\t").append(f.getName()).append(": ");
							what.append(place).append("\n");
						}
					}
				}
			} catch (Throwable t) {
				what.append("No debug info available.");
			}
		}
		return what.toString();
	}
}
