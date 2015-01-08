package net.allochie.vm.jass.ast.dec;

import java.util.ArrayList;

import net.allochie.vm.jass.ast.CodePlace;

public class GlobalsDec extends Dec {

	public ArrayList<VarDec> decs;
	public CodePlace where;

	public GlobalsDec() {
		decs = new ArrayList<VarDec>();
	}

	public void put(VarDec d) {
		decs.add(d);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < decs.size(); i++) {
			result.append(decs.get(i));
			if (i != decs.size() - 1)
				result.append(", ");
		}
		return result.toString();
	}
}
