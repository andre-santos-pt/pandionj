package pt.iscte.pandionj.tests;

public class TestObservable2 {

	interface Observable<ARG> {
		void addObserver(Observer2<ARG> o);
	}
	
	static class A extends Observable2<String> {
		void set() {
			setChanged();
			notifyObservers("?");
		}
	}

	static class B {
		Observable2<Integer> obs = new Observable2<>(); 

		void set() {
			obs.setChanged();
			obs.notifyObservers(3);
		}

		public void addObserver(Observer2<Integer> observer2) {
			obs.addObserver(observer2);
			
		}
	}


	public static void main(String[] args) {
		A a = new A();
		a.addObserver(new Observer2<String>() {

			@Override
			public void update(Observable2<String> obs, String arg) {
				System.out.println(arg);
			}
		});
		a.set();
		
		B b = new B();
		b.addObserver(new Observer2<Integer>() {

			@Override
			public void update(Observable2<Integer> obs, Integer arg) {
				System.out.println(arg);
			}
		});
		b.set();
	}
}
