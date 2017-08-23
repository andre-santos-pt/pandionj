package pt.iscte.pandionj.agent;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class PandionJAgent {

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

		int i = expression.indexOf("(");
		final String expMethod = i == -1 ? "" : expression.substring(0, i);

		inst.addTransformer(new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
				if(s.equals(className)) {
					try {
						ClassPool cp = ClassPool.getDefault();
						CtClass cc = cp.get(s.replace('/','.'));
						CtMethod[] methods = cc.getDeclaredMethods();
						for(CtMethod m : methods) {
							CtClass retType = m.getReturnType();
							if(!m.isEmpty() && m.getName().equals(expMethod) && m.getMethodInfo().isMethod() && !retType.equals(CtClass.voidType)) {
								//								String fieldName = m.getName() + "_return";
								//								CtField f = new CtField(m.getReturnType(), fieldName, cc);
								//								SyntheticAttribute syntheticAttribute = new SyntheticAttribute(cc.getClassFile().getConstPool()); //creating a synthetic attribute using an instance of ConstPool
								//								f.setAttribute(SyntheticAttribute.tag, syntheticAttribute.get());
								//								f.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
								//								cc.addField(f);
//								System.out.println(retType.getComponentType().getName());
								if(retType.isArray() && retType.getComponentType().getName().matches("boolean|byte|short|int|long|char|float|double")) {
									CtClass c = retType;
									while(c.isArray())
										c = c.getComponentType();
									String inst = "System.out.println(\"" + expression + " = \" + java.util.Arrays.toString((" + retType.getComponentType().getName() +"[])$_));";
//									String inst = "Object[] __ret__ = (Object[])$_;";
//									System.out.println(inst);
									m.insertAfter(inst);
								}
								else
									m.insertAfter("System.out.println(\"" + expression + " = \" + $_);");
								//								m.insertAfter(fieldName + " = $_;");
							}
						}

						try {
							// check if real main method exists
							CtMethod method = cc.getMethod("main", "([Ljava/lang/String;)V");
							int mod = method.getModifiers();
							if(!Modifier.isPublic(mod) || !Modifier.isStatic(mod))
								throw new NotFoundException("dummy");

							return null;
						}
						catch(NotFoundException e) {
							//							 String test = "java.io.PrintStream stderr = System.err;" +
							//							 "java.io.OutputStream out = new java.io.OutputStream() { public void write(int b) { } };";
							//							 "System.setErr(new java.io.PrintStream(out));";

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
