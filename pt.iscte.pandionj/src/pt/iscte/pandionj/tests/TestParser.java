package pt.iscte.pandionj.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import pt.iscte.pandionj.parser2.JavaSourceParser;

public class TestParser {
	private JavaSourceParser parser;

	public TestParser(String path) {
		parser = JavaSourceParser.createFromFile(path);
	}

	public TestParser(IFile file) {
		this(file.getLocation().toOSString());
	}

	public void run() {
		parser.parse(new ForStatementVisitor());
	}
	
	
	/////////////////////	Start of Visitor classes	/////////////////////
	///////////////////////////////////////////////////////////////////////// 
	class ForStatementVisitor extends ASTVisitor {
		int numberOfForsFound = 0;
		@Override
		public boolean visit(ForStatement node) {
			writeDebugMessage(node);
			
			ForBodyVisitor bodyVisitor = new ForBodyVisitor();
			node.getBody().accept(bodyVisitor);
			ForExpressionVisitor expVisitor = new ForExpressionVisitor(bodyVisitor);
			node.getExpression().accept(expVisitor);
			
			if(!bodyVisitor.isArrayPrimitiveFigure()) {
				System.out.println("Este ciclo não itera sobre uma array.");
			}
			
			return super.visit(node);
		}
		
		
		private void writeDebugMessage(ForStatement node) {
			numberOfForsFound++;
			System.out.println("\n\n--------------------For numero " + numberOfForsFound + "-------------------");
			System.out.println("for(" + node.initializers().toString() + "; "
							+ node.getExpression().toString() + ";  " + node.updaters().toString() + ")\n"
							+ node.getBody().toString());
		}
	}
	
	
	class ForBodyVisitor extends ASTVisitor{
		private boolean isArrayPrimitiveFigure;
		private List<String> allIterators;
		private HashMap<String, List<String>> iteratorsByArray;
		
		public ForBodyVisitor() {
			isArrayPrimitiveFigure = false;
			allIterators = new ArrayList<>();
			iteratorsByArray = new HashMap<>();
		}
		
		@Override
		public boolean visit(ArrayAccess node) {
			String arrayName = node.getArray().toString();
			List<String> iterators = iteratorsByArray.get(arrayName);
			if(iterators == null) {
				iterators= new ArrayList<>(); 
			}
			
			String iteratorName = node.getIndex().getNodeType() == ASTNode.INFIX_EXPRESSION ?
					filterIteratorName((InfixExpression) node.getIndex()) : node.getIndex().toString();
			if(!iterators.contains(iteratorName)) {
				isArrayPrimitiveFigure = true;
				iterators.add(iteratorName);
				allIterators.add(iteratorName);
				iteratorsByArray.put(arrayName, iterators);
				System.out.println("A variavel " + iteratorName + " está a iterar sobre a array: " + node.getArray().toString());
			}
			
			return super.visit(node);
		}
		
		public boolean isArrayPrimitiveFigure() {
			return isArrayPrimitiveFigure;
		}

		public List<String> getIteratorNames() {
			return allIterators;
		}
		
		// Serve para filtar acessos a arrays como este por exemplo: instanceArray[j - 1]= 2;
		private String filterIteratorName(InfixExpression exp) {
			return exp.getLeftOperand().getNodeType() == ASTNode.SIMPLE_NAME ? exp.getLeftOperand().toString() : exp.getRightOperand().toString();
		}
	}

	
	class ForExpressionVisitor extends ASTVisitor{
		final ForBodyVisitor forBodyVisitor;
		public ForExpressionVisitor(ForBodyVisitor forBodyVisitor) {
			this.forBodyVisitor = forBodyVisitor;
		}
		
		@Override
		public boolean visit(InfixExpression node) {
			if(!forBodyVisitor.isArrayPrimitiveFigure)
				return false;
			
			boolean iteratorOnTheLeft = node.getLeftOperand().getNodeType() == ASTNode.SIMPLE_NAME;
			String varName =  iteratorOnTheLeft ? 
								node.getLeftOperand().toString() : node.getRightOperand().toString();

			if(forBodyVisitor.getIteratorNames().contains(varName)) {
				decodeExpression(varName, iteratorOnTheLeft, node);
			}
			return super.visit(node);
		}
		
		@Override
		public boolean visit(PrefixExpression node) {
			// TODO completar
			System.out.println("PrefixExpression (por completar): " + node.toString());
			return super.visit(node);
		}
		
		@Override
		public boolean visit(PostfixExpression node) {
			// TODO completar
			System.out.println("PostfixExpression (por completar): " + node.toString());
			return super.visit(node);
		}
		
		private void decodeExpression(String varName, boolean iteratorOnTheLet, InfixExpression expression) {
			String operatorName = expression.getOperator().toString();
			String bound = iteratorOnTheLet ? expression.getRightOperand().toString() : expression.getLeftOperand().toString();
			String relacao = new String();
			switch (operatorName) {
			case ">=":
				relacao = iteratorOnTheLet ? "maior ou igual a " : "menor ou igual a ";
				break;
			case "<=":
				relacao = iteratorOnTheLet ? "menor ou igual a " : "maior ou igual a ";
				break;
			case "<":
				relacao = iteratorOnTheLet ? "menor que " : "maior que ";
				break;
			case ">":
				relacao = iteratorOnTheLet ? "maior que " : "menor que ";
				break;
			case "!=":
				relacao = "diferente de ";
				break;
			case "==":
				relacao = "igual a ";
				break;
			default:
				throw new IllegalStateException("Não foi possivel descodificar a condição para terminar o for.");
			}
			String result = "Enquanto " + varName + " for " + relacao + bound;
			
			ForStatement parentNode = (ForStatement) expression.getParent();
			for(Object node : parentNode.updaters()) {
				String iterador = new String();
				String sentido = new String();
				String quantidade = new String();
				if(node instanceof PrefixExpression) {
					PrefixExpression n = (PrefixExpression) node;
					iterador = n.getOperand().toString();
					if(forBodyVisitor.getIteratorNames().contains(iterador)){
						sentido = n.getOperator().toString().equals("++") ? "aumenta" : "decresce";
						quantidade = "1";
					}else {
						continue;
					}
				}
				else if(node instanceof PostfixExpression) {
					PostfixExpression n = (PostfixExpression) node;
					iterador = n.getOperand().toString();
					if(forBodyVisitor.getIteratorNames().contains(iterador)){
						sentido = n.getOperator().toString().equals("++") ? "aumenta" : "decresce";
						quantidade = "1";
					}else {
						continue;
					}
				}
				else if(node instanceof Assignment) {
					Assignment n = (Assignment) node;
					iterador = n.getLeftHandSide().toString();
					if(forBodyVisitor.getIteratorNames().contains(iterador)){
						sentido = n.getOperator().toString().equals("+=") ? "aumenta" : "decresce";
						quantidade = n.getRightHandSide().toString();
					}else {
						continue;
					}
				}
				result += ", o iterador " + iterador + " " + sentido + " por " + quantidade + " a cada iteração.";
			}
			
			System.out.println(result);
		}
	}
	
	/////////////////////	End of Visitor classes	/////////////////////
	///////////////////////////////////////////////////////////////////// 
	
	
	int[] instanceArray = new int[10];
	
	@SuppressWarnings("unused")
	void test1() {
		for(boolean b = false; !b;) {
			b = true;
		}
		
		int[] localArray = new int[] {10,9,8,7,6,5,4,3,2,1};
		
		// ciclo for que manipula array local, com condicao que tende para uma constante
		for(int i = 0; i < 10; i++) {
			localArray[i] = i;
		}
		
		// ciclo for que manipula array da instancia, com condicao que tende para uma constante
		for(int i = 9; 0 <= i; i--) {
			instanceArray[i] = i;
		}
		
		// ciclo for que manipula array da instancia, com condicao que tende para uma variavel
		int i = -1;
		for(int j = 10; j > i; j--) {
			instanceArray[j - 1] = i - j;
			i += 2;
		}
		
		// ciclo for com variavel de controlo inicializada fora do ciclo
		i = 0;
		for(; i < 10; i++) {
			localArray[i] = 10 - i;
		}
		
		// ciclo for que itera sobre uma array mas nao lhe altera os valores
		for(int j = 10; j > i; j--) {
			int b = localArray[j];
		}
		
		// ciclo for que não itera sobre uma array
		for(int j = 0; j < 10; j++) {
			int b = j + j;
			b = b > j ? b : j;
		}
		
		// ciclo for com condicao de terminar com sinal !=
		for(int j = 10; j != i; j--) {
			int b = localArray[j];
		}
		
		// ciclo for com condicao de terminar com sinal ==, e incremento diferente de 1
		for(int j = 10; j != i; i++,j-=10) {
			int b = localArray[j];
		}
		
		// ciclo for que manipula as duas arrays, com condicao que tende para uma variavel
		for(int j = 10; j > i; j--, i += 2) {
			instanceArray[j - 1] = i - j;
			localArray[i- 2 * j] = 0;
		}
	}
	

	public static void main(String[] args) {
		TestParser parser = new TestParser("C:\\Users\\André Freire\\git\\pandionj\\pt.iscte.pandionj\\src\\pt\\iscte\\pandionj\\tests\\TestParser.java");
		parser.run();
	}
}