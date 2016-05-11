import org.checkerframework.checker.units.qual.*;

public class OverrideMethods {
	@Override
	public String toString() {
		return "blah";
	}
}

class Test {
	void m() {
		OverrideMethods x = new OverrideMethods();
		x.toString();

		@m Object y = new @m Object();
		y.toString();


	}
}
