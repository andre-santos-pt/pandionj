package pt.iscte.pandionj.tests;

import pt.iscte.pandionj.parser2.VarParser;

class T {
	
}

class Test {
	class Inner {
		int xxx;
	}
	int param, param2;
	
	void test1(int param) {
		int[] v = new int[3];
		int[] w = new int[3];

		int i = 0, jj = 2;
		v[i] = 8;
		v[2] = 9;

		int sum = 0;
		while(i < jj) {
			i = i + 1;
			sum += v[i];
		}
		
		for(int j = 0, u=1; j < i; j++) { "ss".length(); } 
		
		for(int j = v.length-1; j > 0; j--) v[j] = 0;
//
		int[][] m = new int[10][10];
		for(int y = 0; y < 10; y++)
			for(int x = 0; x < y; x++)
				m[y][x] = 1;
		
		if(true){
			int x = 0;
			x-=2;
		}
		{
			int x = 0;
			switch(x) {
			case 0: i++;
			}
		}
	}

	void test2(int param) { 

	}

	public static void main(String[] args) {

		VarParser parser = new VarParser("/Users/andresantos/git/pandionj2/pt.iscte.pandionj/src/pt/iscte/pandionj/tests/Test.java");
		parser.run();
		System.out.println(parser.toText());
		System.err.println(parser.locateVariable("j", 29));
		
	}
}