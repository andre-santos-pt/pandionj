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

public class PandionJAgent {

	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new ClassFileTransformer() {
			@Override
			public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
				if(s.equals(agentArgs)) {
					try {
						ClassPool cp = ClassPool.getDefault();

						CtClass cc = cp.get(s.replace('/','.'));


//						CtMethod[] methods = cc.getDeclaredMethods();
//						for(CtMethod m : methods) {
//							if(!m.isEmpty() && m.getMethodInfo().isMethod() && !m.getReturnType().equals(CtClass.voidType))
//								//								m.insertAfter("System.out.println(\"?\");");
//								m.insertAfter("System.out.println(\"" + m.getName() + "() = \" + $_);");
//						}
//
//						try {
//							// check is main method exists
//							CtMethod fakeMain = cc.getMethod("main", "()V");
//							fakeMain.setModifiers(Modifier.PUBLIC|Modifier.STATIC);
//
//							CtClass ctClass = cp.get("java.lang.String[]");
//							fakeMain.addParameter(ctClass);
//
//
//							byte[] byteCode = cc.toBytecode();	
//							cc.detach();
//
//							System.out.println(fakeMain);
//							return byteCode;
//						}
//						catch(NotFoundException e) {
//							System.err.println("fake main");
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
							try {
								CtMethod fakeMain = cc.getMethod("main", "()V");
								if(!Modifier.isStatic(fakeMain.getModifiers()))
									throw new NotFoundException("dummy");
								
								fakeMain.setModifiers(Modifier.PUBLIC|Modifier.STATIC);

								CtClass ctClass = cp.get("java.lang.String[]");
								fakeMain.addParameter(ctClass);


								byte[] byteCode = cc.toBytecode();	
								cc.detach();
								return byteCode;
							}
							catch(NotFoundException ex) {
								return null;
							}


						}
						////							CtField f = CtField.make("private static final int z = 0;", cc);
						////							f.setAttribute(SyntheticAttribute.tag, null);
						////							cc.addField(f);
						//							
						//							CtMethod m = CtNewMethod.make("public static void main(String[] args) { }", cc);
						//							cc.addMethod(m); 
						////							CtConstructor classInitializer = cc.getClassInitializer();
						////							classInitializer.addLocalVariable("zz", CtClass.intType);
						////							if(classInitializer != null) {
						////								classInitializer.insertAfter("System.out.println(\"exit\");");
						////								classInitializer.insertAfter("System.exit(0);");
						////							}
						//							byte[] byteCode = cc.toBytecode();	
						//							cc.detach();
						//							return byteCode;
						//						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				return null;
			}
		});
	}
}