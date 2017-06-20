package pt.iscte.pandionj.agent;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.eclipse.swt.internal.C;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.SyntheticAttribute;

public class PandionJAgent2 {

	private static String className;
	private static String expression;

	public static void premain(String agentArgs, Instrumentation inst) {
		String[] split = agentArgs.split(";");
		if(split.length != 2)
			className = agentArgs;
		else {
			className = split[0];
			expression = split[1];
		}
		System.out.println(expression);
		inst.addTransformer(new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
				if(s.equals(className)) {
					try {
						ClassPool cp = ClassPool.getDefault();
						CtClass cc = cp.get(s.replace('/','.'));
						// TODO overloading
//						CtMethod[] methods = cc.getDeclaredMethods();
//						for(CtMethod m : methods) {
//							if(!m.isEmpty() && m.getMethodInfo().isMethod() && !m.getReturnType().equals(CtClass.voidType)) {
//								String fieldName = m.getName() + "_return";
//								CtField f = new CtField(m.getReturnType(), fieldName, cc);
//								SyntheticAttribute syntheticAttribute = new SyntheticAttribute(cc.getClassFile().getConstPool()); //creating a synthetic attribute using an instance of ConstPool
//								f.setAttribute(SyntheticAttribute.tag, syntheticAttribute.get());
//								f.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
//								cc.addField(f);
//								m.insertAfter("System.out.println(\"" + m.getName() + "() = \" + $_);");
//								m.insertAfter(fieldName + " = $_;");
//							}
//						}
						
						try {
							// check if real main method exists
							CtMethod method = cc.getMethod("main", "([Ljava/lang/String;)V");
							int mod = method.getModifiers();
							if(!Modifier.isPublic(mod) || !Modifier.isStatic(mod))
								throw new NotFoundException("dummy");

							return null;
						}
						catch(NotFoundException e) {
							CtMethod m = CtNewMethod.make("public static void main(String[] args) { " + expression + "; }", cc);
							cc.addMethod(m); 

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