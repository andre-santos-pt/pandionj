package pt.iscte.pandionj.agent;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

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
						CtClass retType = null;
						boolean multiple = false;
						for(CtMethod m : methods) {
							if(!m.isEmpty() && m.getName().equals(expMethod) && m.getMethodInfo().isMethod() && !m.getReturnType().equals(CtClass.voidType)) {
								if(retType != null)
									multiple = true;
								retType = m.getReturnType();
							}
						}
						if(multiple)
							retType = null;

						try {
							// check if real main method exists
							CtMethod method = cc.getMethod("main", "([Ljava/lang/String;)V");
							int mod = method.getModifiers();
							if(!Modifier.isPublic(mod) || !Modifier.isStatic(mod))
								throw new NotFoundException("dummy");

							return null;
						}
						catch(NotFoundException e) {
							CtMethod m = CtNewMethod.make("public static void main(String[] args) {  }", cc);
							cc.addMethod(m); 

							if(retType == null) {
								m.insertAfter(expression + ";");
							}
							else if(retType.isArray() && retType.getComponentType().getName().matches("boolean|byte|short|int|long|char|float|double")) {
								String inst = "System.out.println(\"" + expression + " = \" + java.util.Arrays.toString((" + retType.getComponentType().getName() +"[])" + expression + "));";
								//								String inst = "Object[] __ret__ = (Object[])$_;";
								//								System.out.println(inst);
								m.insertAfter(inst);
							}
							else if(retType.isArray() && getNDims(retType) == 2) {
								String inst = retType.getName() + " ret = " + expression + ";\n";
								inst += "System.out.print(\"" + expression + " = [\");";
								String cName = retType.getName();
								cName = cName.substring(0, cName.length()-2);
								inst += "for(int i = 0; i < ret.length; i++) {";
								inst += "String s = java.util.Arrays.toString(ret[i]);";
								inst += "if(i != 0) System.out.print(\", \");";
								inst += "System.out.print(s);};";
								inst += "System.out.println(\"]\");";
								m.insertAfter(inst);
							}
							else if(!CtClass.voidType.equals(retType)){
								String inst = "System.out.println(\"" + expression + " = \" + " + expression + ");";
								m.insertAfter(inst);
							}

							//								m.insertAfter(fieldName + " = $_;");

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

			private int getNDims(CtClass retType) {
				CtClass c = retType;
				int d = 0;
				while(c.isArray()) {
					try {
						c = c.getComponentType();
					} catch (NotFoundException e) {
						e.printStackTrace();
					}
					d++;
				}
				return d;
			}
		});
	}
}
