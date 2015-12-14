package net.allochie.vm.rssl.compiler;

import java.io.IOException;

public class Opcode {

	public Opcodes op;
	public AZAccess[] words;

	public void readFromStream(BVMOpcodeStream bvmOpcodeStream) throws IOException {
		words = new AZAccess[op.width];
		for (int i = 0; i < words.length; i++)
			words[i] = new AZAccess(bvmOpcodeStream.nextQWord());
	}

	public void writeToStream(BVMOpcodeStream bvmOpcodeStream) throws IOException {
		for (int i = 0; i < words.length; i++)
			bvmOpcodeStream.writeQWord(words[i].value);
	}

}
