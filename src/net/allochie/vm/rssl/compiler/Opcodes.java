package net.allochie.vm.rssl.compiler;

public enum Opcodes {

	/** Do nothing **/
	NOP(0x00, 0),
	/** Move A1 to A0 **/
	MOVE(0x01, 2),
	/** Load constant A1 to A0 **/
	LDCON(0x02, 2),
	/** Load null from register A0 to A1 inclusive **/
	LDNUL(0x03, 2),
	/** Load global var A1 to A0 **/
	LDGLO(0x04, 2),
	/** Load array pointer A1 to A0 **/
	LDARR(0x05, 2),
	/** Save array pointer A1 to A0 **/
	SVARR(0x06, 2),
	/** Load index A1 from array pointer A2 into A0 **/
	LARRI(0x07, 3),
	/** Save A0 into index A1 from array pointer A2 **/
	SARRI(0x08, 3),
	/** Load function reference with label A1 into A0 */
	LDPTR(0x09, 2),
	/** Swap A0 with A1, A1 with A0 (preserves A0 & A1) **/
	SWAP(0x0A, 2),
	/** Add A1 to A2, store in A0 **/
	ADD(0x0B, 3),
	/** Sub A1 from A2, store in A0 **/
	SUB(0x0C, 3),
	/** Multiply A1 by A2, store in A0 **/
	MUL(0x0D, 3),
	/** Divide A1 by A2, store in A0 **/
	DIV(0x0E, 3),
	/** Modulus A1 by A2, store in A0 **/
	MOD(0x0F, 3),
	/** Raise A1 by A2, store in A0 **/
	POW(0x10, 3),
	/** Negate A1, store in A0 **/
	UNM(0x11, 2),
	/** Boolean NOT A1, store in A0 **/
	NOT(0x12, 2),
	/** Jump to relative PC offset A0 (PC += A0) **/
	JMPRO(0x13, 1),
	/**
	 * Call symbol with label A0 with parameter range A1 to A2 inclusive, store
	 * return in A3
	 **/
	CALL(0x14, 4),
	/**
	 * Call symbol with label A0 with parameter range A1 to A2 inclusive, ignore
	 * return result
	 **/
	ICALL(0x15, 3),
	/**
	 * Tail-call symbol with label A0 with parameter range A1 to A2 inclusive;
	 * implicitly RETURN result or THROW ex
	 **/
	TCALL(0x16, 3),
	/** Return A0 **/
	RETN(0x17, 1),
	/** Yields A0 **/
	YIELD(0x18, 1),
	/** Throws A0 **/
	THROW(0x19, 1),
	/** Sets address of next exception handler **/
	SETEF(0x1A, 1),
	/** Sets core debug flag to A0 **/
	DEBUG(0x1B, 1),
	/** Raises breakpoint code A0 + A1 **/
	TCL(0x1C, 2),
	/** Compare A0 > A1, skip next instruction if true **/
	GT(0x1D, 2),
	/** Compare A0 >= A1, skip next instruction if true **/
	GTEQ(0x1E, 2),
	/** Compare A0 < A1, skip next instruction if true **/
	LT(0x1F, 2),
	/** Compare A0 <= A1, skip next instruction if true **/
	LTEQ(0x20, 2),
	/** Compare A0 = A1, skip next instruction if true **/
	EQ(0x21, 2),
	/** Compare A0 <> A1, skip next instruction if true **/
	NEQ(0x22, 2),
	/** Coerce A0 to boolean, boolean and A0/A1, skip next instruction if true **/
	TEST(0x23, 2);

	public static Opcodes from(byte dword) {
		for (Opcodes op : Opcodes.values())
			if (op.opword == dword)
				return op;
		return null;
	}

	public final byte opword;
	public final int width;

	Opcodes(int opword, int width) {
		this.opword = (byte) opword;
		this.width = width;
	}
}
