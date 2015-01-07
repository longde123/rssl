package net.allochie.vm.jass.debug;

import net.allochie.vm.jass.api.IDebugger;

public class AllDebugger implements IDebugger {

	@Override
	public void trace(Object... args) {
		StringBuilder s = new StringBuilder();
		s.append("trace: ");
		for (int i = 0; i < args.length; i++)
			s.append(args[i]).append(", ");
		System.out.println(s.toString());
	}

	@Override
	public void fatal(Object... args) {
		StringBuilder s = new StringBuilder();
		s.append("fatal: ");
		for (int i = 0; i < args.length; i++)
			s.append(args[i]).append(", ");
		System.out.println(s.toString());
	}

}
