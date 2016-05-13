package net.allochie.vm.rssl.compiler;

import java.io.IOException;

public class Opcode {

	public Opcodes op;
	public AZAccess[] words;

	public Opcode() {
	}

	public Opcode(Opcodes opcode) {
		this.op = opcode;
	}

	public Opcode(Opcodes opcode, int regs) {
		this.op = opcode;
		this.words = new AZAccess[regs];
	}

	public void readFromStream(BVMStream bvmOpcodeStream) throws IOException {
		words = new AZAccess[op.width];
		for (int i = 0; i < words.length; i++)
			words[i] = new AZAccess(bvmOpcodeStream.nextQWord());
	}

	public void writeToStream(BVMStream bvmOpcodeStream) throws IOException {
		for (int i = 0; i < words.length; i++)
			bvmOpcodeStream.writeQWord(words[i].value);
	}

}
