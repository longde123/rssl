package net.allochie.vm.jass.ast;

import java.util.ArrayList;

import net.allochie.vm.jass.ast.dec.Dec;

public class JASSFile {

	public ArrayList<Dec> decs;
	public ArrayList<Function> funcs;

	public JASSFile() {
		this.decs = new ArrayList<Dec>();
		this.funcs = new ArrayList<Function>();
	}

	public void put(Dec d) {
		this.decs.add(d);
	}

	public void put(Function f) {
		this.funcs.add(f);
	}

}
