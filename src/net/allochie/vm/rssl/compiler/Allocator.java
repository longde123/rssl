package net.allochie.vm.rssl.compiler;

import java.util.HashMap;

import net.allochie.vm.rssl.ast.Type;
import net.allochie.vm.rssl.runtime.value.VMFunctionPointer;

public class Allocator {

	private HashMap<Integer, String> regIdx = new HashMap<Integer, String>();
	private HashMap<String, Integer> regName = new HashMap<String, Integer>();

	private HashMap<Integer, Object> consts = new HashMap<Integer, Object>();
	private HashMap<Integer, Type> constsTypes = new HashMap<Integer, Type>();
	private HashMap<Object, Integer> constsVals = new HashMap<Object, Integer>();

	private volatile int lastReg = 0;
	private volatile int lastConst = 0;
	
	private volatile int ptrSRegs = 0;
	private volatile int lastSReg = 0;
	private volatile int maxSRegs = 0;

	public Allocator(int maxSRegs) {
		this.maxSRegs = maxSRegs;
		if (maxSRegs != 0)
			ptrSRegs = giveRangeNext("ScratchRegister", maxSRegs);
	}

	public int getOrAddConst(Object o) throws RSSLCompilerException {
		Type t = getTypeOf(o);
		int slot = getConst(o);
		if (slot == 0)
			return addConst(o);
		if (constsTypes.get(slot) != t)
			return addConst(o);
		return slot;
	}

	public int getConst(Object o) throws RSSLCompilerException {
		return constsVals.get(o);
	}

	public int addConst(Object o) throws RSSLCompilerException {
		Type t = getTypeOf(o);
		int me = lastConst;
		lastConst++;
		consts.put(me, o);
		constsTypes.put(me, t);
		constsVals.put(o, me);
		return me;
	}

	private Type getTypeOf(Object z) throws RSSLCompilerException {
		if (z == null || z instanceof Void || z instanceof Void[])
			return Type.nullType;
		if (z instanceof Integer || z instanceof Integer[])
			return Type.integerType;
		if (z instanceof Float || z instanceof Float[] || z instanceof Double || z instanceof Double[])
			return Type.realType;
		if (z instanceof String || z instanceof String[])
			return Type.stringType;
		if (z instanceof Boolean || z instanceof Boolean[])
			return Type.booleanType;
		if (z instanceof VMFunctionPointer || z instanceof VMFunctionPointer[])
			return Type.codeType;
		throw new RSSLCompilerException("Unable to find base type for " + z.getClass());
	}

	public int giveNext(String why) {
		int mine = lastReg;
		lastReg++;
		regIdx.put(mine, why);
		regName.put(why, mine);
		return mine;
	}

	public int giveRangeNext(String why, int count) {
		int mine = lastReg;
		lastReg += count;
		for (int i = 0; i < count; i++) {
			regIdx.put(mine + i, why + i);
			regName.put(why + i, mine + i);
		}
		return mine;
	}

	public void free(int which) {
		regName.remove(regIdx.remove(which));
	}

	public int findRegister(String which) {
		return regName.get(which);
	}

	public int nextScratchRegister() {
		lastSReg++;
		if (lastSReg >= maxSRegs) 
			lastSReg -= maxSRegs;
		return ptrSRegs + lastSReg;
	}
	
	public int lastScratchRegister() {
		return ptrSRegs + lastSReg;
	}

}
