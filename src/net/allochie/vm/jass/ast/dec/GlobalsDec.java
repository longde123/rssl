package net.allochie.vm.jass.ast.dec;

import java.util.ArrayList;

public class GlobalsDec extends Dec {

	public ArrayList<VarDec> decs;

	public GlobalsDec() {
		this.decs = new ArrayList<VarDec>();
	}

	public void put(VarDec d) {
		this.decs.add(d);
	}
}
