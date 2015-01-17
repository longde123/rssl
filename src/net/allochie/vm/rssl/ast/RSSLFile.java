package net.allochie.vm.rssl.ast;

import java.util.ArrayList;

import net.allochie.vm.rssl.ast.dec.Dec;

public class RSSLFile {

	/** File declaration heap */
	public ArrayList<Dec> decs;
	/** File function heap */
	public ArrayList<Function> funcs;

	public RSSLFile() {
		decs = new ArrayList<Dec>();
		funcs = new ArrayList<Function>();
	}

	public void put(Dec d) {
		decs.add(d);
	}

	public void put(Function f) {
		funcs.add(f);
	}

}
