package sample1;

public class TestInheritance1 extends TestInheritance2 {
    @Override
    public int test1() {
        return -7;
    }

    @Override
    public int test2() {
        return -super.test2();
    }
}
