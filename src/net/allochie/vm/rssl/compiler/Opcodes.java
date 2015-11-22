package net.allochie.vm.rssl.compiler;

public enum Opcodes {

	NOP(0x00, 0), /* NOP do nothing */

	MOV(0x01, 2), /* MOV move A=>B */

	LDC(0x02, 2), /* LDC load const=>B */
	LDN(0x03, 2), /* LDN load null REG(A=>B) */
	LDG(0x04, 2), /* LDS load global=>B */
	LDA(0x05, 2), /* LDA load arrayptr=>B */

	SVA(0x06, 2), /* SVA sav A=>arrayptr */

	ADD(0x07, 2), /* ADD A+B => A */
	SUB(0x08, 2), /* SUB A-B => A */
	MUL(0x09, 2), /* MUL AxB => A */
	DIV(0x0A, 2), /* DIV A/B => A */
	MOD(0x0B, 2), /* MOD A%B => A */
	POW(0x0C, 2), /* POW A^B => A */

	UNM(0x0D, 1), /* UNM ~A => A */
	NOT(0x0E, 1), /* NOT !A => A */

	JRO(0x0F, 1); /* JRO A=>(PTR+A) */

	public final byte opword;
	public final int width;

	Opcodes(int opword, int width) {
		this.opword = (byte) opword;
		this.width = width;
	}
}
