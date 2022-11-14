package pt.iscte.pandionj.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Scanner;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class PandionJAgentScript {

	public static void premain(String agentArgs, Instrumentation inst) {
		Scanner scanner = new Scanner(agentArgs);
		String className = scanner.nextLine();
		String script = readScript(scanner);
		scanner.close();
		
		inst.addTransformer(new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass,
					ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
				if (s.equals(className)) {
					ClassPool cp = ClassPool.getDefault();
					try {
						CtClass cc = cp.get(s.replace('/', '.'));
						try {
							// check if real main method exists
							CtMethod mainMethod = cc.getMethod("main", "([Ljava/lang/String;)V");
							int mod = mainMethod.getModifiers();
							if (!Modifier.isPublic(mod) || !Modifier.isStatic(mod))
								throw new NotFoundException("dummy");

							return null;
						} catch (NotFoundException e) {

							generateMain(cc, script);

							byte[] byteCode = cc.toBytecode();
							cc.detach();
							return byteCode;
						}
					} catch (Exception ex) {
						System.err.println("Could not find class: " + className);
						ex.printStackTrace();
					}
				}
				return null;
			}

			private void generateMain(CtClass cc, String script) throws CannotCompileException, NotFoundException {
				CtMethod m = CtNewMethod.make("public static void main(String[] args) { }", cc);
				cc.addMethod(m);

				m.insertAfter(script);

				m.insertAfter("System.exit(0);");
			}
		});
	}
	
	private static String readScript(Scanner scanner) {
		String script = "";
		while(scanner.hasNextLine())
			script += scanner.nextLine() + "\n";
		return script;
	}
}
