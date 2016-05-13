package net.allochie.vm.rssl.compiler.analysis;

import java.util.HashMap;

import net.allochie.vm.rssl.ast.CodePlace;
import net.allochie.vm.rssl.ast.Param;
import net.allochie.vm.rssl.ast.dec.TypeDec;
import net.allochie.vm.rssl.ast.dec.VarDec;
import net.allochie.vm.rssl.compiler.RSSLCompilerException;

public class CFClosure {

	public HashMap<String, VarDec> lvars = new HashMap<String, VarDec>();
	public final CFClosure parent;

	public CFClosure() {
		parent = null;
	}

	public CFClosure(CFClosure parent) {
		this.parent = parent;
	}

	public VarDec get(String label) {
		if (lvars.containsKey(label))
			return lvars.get(label);
		if (parent == null)
			return null;
		return parent.get(label);
	}

	public boolean existsInLocal(String label) {
		if (lvars.containsKey(label))
			return true;
		if (parent == null)
			return false;
		return parent.existsInLocal(label);
	}

	public void checkExistsInLocal(String label) throws RSSLCompilerException {
		if (!existsInLocal(label))
			throw new RSSLCompilerException("No such local variable " + label);
	}

	public void checkNotExistsInLocal(String label) throws RSSLCompilerException {
		if (existsInLocal(label))
			throw new RSSLCompilerException("Existing local variable " + label);
	}

	public void createVarInLocal(VarDec dec) throws RSSLCompilerException {
		checkNotExistsInLocal(dec.name.image);
		lvars.put(dec.name.image, dec);
	}

	public void createVarInLocal(CodePlace where, Param dec) throws RSSLCompilerException {
		checkNotExistsInLocal(dec.name.image);
		lvars.put(dec.name.image, transformToVarDec(where, dec));
	}

	private VarDec transformToVarDec(CodePlace where, Param dec) {
		VarDec aresult = new VarDec();
		aresult.array = dec.array;
		aresult.type = dec.type;
		aresult.where = where;
		return aresult;
	}

	public VarDec getCheckedInLocal(String label) throws RSSLCompilerException {
		checkExistsInLocal(label);
		return get(label);
	}

}
