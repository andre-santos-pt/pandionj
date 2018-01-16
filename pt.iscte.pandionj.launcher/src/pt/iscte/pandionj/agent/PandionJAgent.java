package pt.iscte.pandionj.agent;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class PandionJAgent {

	private static String className;
	private static String expression;
	private static String methodSig;

	public static void premain(String agentArgs, Instrumentation inst) {
		String[] split = agentArgs.split("\\|");
		if(split.length != 3)
			className = agentArgs;
		else {
			className = split[0];
			expression = split[1];
			methodSig = split[2];
		}
		inst.addTransformer(new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
				if(s.equals(className)) {
					ClassPool cp = ClassPool.getDefault();
					try {
						CtClass cc = cp.get(s.replace('/','.'));
						try {
							// check if real main method exists
							CtMethod mainMethod = cc.getMethod("main", "([Ljava/lang/String;)V");
							int mod = mainMethod.getModifiers();
							if(!Modifier.isPublic(mod) || !Modifier.isStatic(mod))
								throw new NotFoundException("dummy");

							return null;
						}
						catch(NotFoundException e) {
							if(expression == null) {
								CtMethod m = CtNewMethod.make("public static void main(String[] args) {  }", cc);
								cc.addMethod(m);
//								CtConstructor init = cc.getClassInitializer();
//								init.insertAfter("System.out.print(\"\");");
							}
							else {
								int i = expression.indexOf("(");	
								final String methodName = i == -1 ? "" : expression.substring(0, i);
								CtMethod method = null;
								try {
									method = cc.getMethod(methodName, methodSig);
								}
								catch(NotFoundException ex) {
									System.err.println("Could not find method: " + className + "." + methodName + " " + methodSig);
									CtMethod m = CtNewMethod.make("public static void main(String[] args) {  }", cc);
									cc.addMethod(m); 
									byte[] byteCode = cc.toBytecode();	
									cc.detach();
									return byteCode;
								}
								CtClass retType = method.getReturnType();

								generateMain(cc, retType);
							}
							// TODO future: field for loose instances
							//							CtField f = new CtField(cp.get("java.lang.String"),"test",cc);
							//							f.setModifiers(Modifier.STATIC | Modifier.PUBLIC);
							//							cc.addField(f);

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

			private void generateMain(CtClass cc, CtClass retType) throws CannotCompileException, NotFoundException {
				CtMethod m = CtNewMethod.make("public static void main(String[] args) { }", cc);
				cc.addMethod(m); 

				String left = expression.replaceAll("\"", "\\\\\"");
				String right = expression;

				if(retType.equals(CtClass.voidType)) {
					m.insertAfter(expression + ";");
				}
				else if(retType.isArray() && retType.getComponentType().getName().matches("boolean|byte|short|int|long|char|float|double")) {
					String inst = "System.out.println(\"" + left + " = \" + java.util.Arrays.toString((" + retType.getComponentType().getName() +"[])" + right + "));";
					m.insertAfter(inst);
				}
				else if(retType.isArray() && getNDims(retType) == 2) {
					String inst = retType.getName() + " ret = " + left + ";\n";
					inst += "System.out.print(\"" + left + " = [\");";
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
					String inst = null;
					if(CtClass.charType.equals(retType))
						inst = "System.out.println(\"" + left + " = '\" + " + right + " + \"'\");";
					else if(retType.getName().equals(String.class.getName()))
						inst = "System.out.println(\"" + left + " = \\\"\" + " + right + " + \"\\\"\");";
					else
						inst = "System.out.println(\"" + left + " = \" + " + right + ");";

					m.insertAfter(inst);
				}

				m.insertAfter("System.exit(0);");
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
