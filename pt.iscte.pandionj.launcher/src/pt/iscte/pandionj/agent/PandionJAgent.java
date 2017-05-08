package pt.iscte.pandionj.agent;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class PandionJAgent {

	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
				if(!s.startsWith("java/") && !s.startsWith("sun/")) {
					try {
						ClassPool cp = ClassPool.getDefault();

						CtClass cc = cp.get(s.replace('/','.'));
						try {
							// check is main method exists
							cc.getMethod("main", "([Ljava/lang/String;)V");
							return null;
						}
						catch(NotFoundException e) {
							CtMethod m = CtNewMethod.make("public static void main(String[] args) { }", cc);
							cc.addMethod(m); 

							CtConstructor classInitializer = cc.getClassInitializer();
							if(classInitializer != null)
								classInitializer.insertAfter("System.exit(0);");

							byte[] byteCode = cc.toBytecode();	
							cc.detach();
							return byteCode;
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				return null;
			}
		});
	}
}