package net.allochie.vm.rssl;

import java.lang.reflect.Field;
import java.util.ArrayList;

import net.allochie.vm.rssl.ast.CodePlace;

public class VMException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -4576283011705444688L;
	public Object what;
	public ArrayList<Object> frames = new ArrayList<Object>();

	public VMException(Object what, String reason) {
		super(reason);
		this.what = what;
	}

	public VMException(Object what, String reason, Throwable t) {
		super(reason, t);
		this.what = what;
	}

	@Override
	public String toString() {
		StringBuilder what = new StringBuilder();
		what.append(getMessage()).append("\n");
		what.append(findCodePoint()).append("\n");
		return what.toString();
	}

	public String generateMessage(RSSLMachine jassMachine, RSSLThread thread) {
		StringBuilder result = new StringBuilder();
		result.append(toString());
		result.append("\t").append("in thread: ").append(thread.name).append("\n");
		result.append("\t").append("in machine: ").append(jassMachine.getName());
		return result.toString();
	}

	public void pushFrame(VMStackFrame f0) {
		frames.add(f0);
	}

	public String findCodePoint() {
		StringBuilder top = new StringBuilder();
		top.append("at ").append(findLabel(what)).append("\n");
		for (int i = 0; i < frames.size(); i++)
			top.append(" in section ").append(findLabel(frames.get(i))).append("\n");
		return top.toString().trim();
	}

	public String findLabel(Object what) {
		if (what != null)
			try {
				Class<?> thing = what.getClass();
				String place = thing.getName() + ", ??:?? (java)";
				for (Field f : thing.getFields())
					if (f.getType().equals(CodePlace.class)) {
						f.setAccessible(true);
						CodePlace where = (CodePlace) f.get(what);
						if (where != null)
							place = where.toString();
						if (f.getName().equals("where"))
							return place;
					}
				return place;
			} catch (Throwable t) {
				return "unknown, ??:?? (" + t.toString() + ")";
			}
		else
			return "<noval>, ??:??";
	}
}
