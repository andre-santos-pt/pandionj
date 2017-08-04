package pt.iscte.pandionj.tests;

import pt.iscte.pandionj.parser.VarParser;

class T {
	
}


public class Test {
	int max(int[] v) {
		int max = 0;
		int i = 0;
		while(i < (v.length)) {
		if(v.length > 0)
			max = 2;
		}
		
		return max;
	}

	
	class Inner {
		int xxx;
	}
	int att, att2;
	
	void test1(int param) {
		int[] v = new int[3];
		swap(v, 0, 2);
//		int[] w = new int[3];
//
		int i = 0, jj = 2;
//		v[i] = 8;
//		v[2] = 9;
//
		int sum = 0;
		while(i <= jj && i != (jj+1)) {
			--i;
			sum = sum + v[i-1];
		}
//		
//		for(int j = 0, u=1; j < i; j++) { "ss".length(); } 
//		
//		for(int j = v.length-1; j > 0; j--) v[j] = 0;
//
		int[][] m = new int[10][10];
		for(int y = 0; y < 10; y = y + 1)
			for(int x = 0; x < y; ++x) {
				m[y][x] = 1;
				v[x] = 1;
			}
		
//		if(true){
//			int x = 0;
//			x-=2;
//		}
//		{
//			int x = 0;
//			switch(x) {
//			case 0: i++;
//			}
//		}
	}

	static void swap(int[] v, int i, int j) { 
		int t = v[i];
		v[i] = t;
		v[j] = t;
	}
	
	static int sumInterval(int[] v, int a, int b) {
		swap(v, a, b);
		int sum = 0;
		for(int i = a; i <= b; i++)
			sum += v[i];
		return sum;
	}
	
	public static void main(String[] args) {

		VarParser parser = new VarParser("src/pt/iscte/pandionj/tests/Test.java");
		parser.run();
//		System.out.println(parser.toText());
		parser.print();
		System.err.println(parser.locateVariable("j", 29));
		
	}
}