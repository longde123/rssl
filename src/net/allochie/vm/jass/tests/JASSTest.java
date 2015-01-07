package net.allochie.vm.jass.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.allochie.vm.jass.JASSMachine;
import net.allochie.vm.jass.JASSThread;
import net.allochie.vm.jass.VMCallFrame;
import net.allochie.vm.jass.VMClosure;
import net.allochie.vm.jass.VMException;
import net.allochie.vm.jass.VMFunctionPointer;
import net.allochie.vm.jass.ast.Function;
import net.allochie.vm.jass.ast.JASSFile;
import net.allochie.vm.jass.ast.Statement;
import net.allochie.vm.jass.ast.StatementList;
import net.allochie.vm.jass.ast.dec.Dec;
import net.allochie.vm.jass.ast.statement.ConditionalStatement;
import net.allochie.vm.jass.ast.statement.LoopStatement;
import net.allochie.vm.jass.debug.AllDebugger;
import net.allochie.vm.jass.global.NativeMethodRegistry;
import net.allochie.vm.jass.global.TypeRegistry;
import net.allochie.vm.jass.natives.NativeThreading;
import net.allochie.vm.jass.natives.NativeTypeCasts;
import net.allochie.vm.jass.parser.JASSParser;
import net.allochie.vm.jass.parser.ParseException;
import net.allochie.vm.jass.types.BooleanFuncWrapper;

public class JASSTest {

	public static void main(String[] args) {
		try {
			JASSParser parse = new JASSParser(new FileInputStream("rt.jass"));
			JASSFile file = parse.file();

			NativeMethodRegistry.registerNativeMethodProvider(NativeMethodDemo.class);

			TypeRegistry.registerTypeWithClass("boolexpr", BooleanFuncWrapper.class);
			NativeMethodRegistry.registerNativeMethodProvider(BooleanFuncWrapper.class);

			TypeRegistry.registerTypeWithClass("thread", JASSThread.class);
			NativeMethodRegistry.registerNativeMethodProvider(NativeThreading.class);
			NativeMethodRegistry.registerNativeMethodProvider(NativeTypeCasts.class);

			JASSMachine machine = new JASSMachine("Demo machine");
			machine.setDebugger(new AllDebugger());
			try {
				JASSThread main = machine.allocateThread("main", new VMFunctionPointer("main"));
				main.doFile(file);
				main.runThread();
				machine.putThread(main);
				machine.start();
			} catch (VMException e) {
				e.printStackTrace();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
