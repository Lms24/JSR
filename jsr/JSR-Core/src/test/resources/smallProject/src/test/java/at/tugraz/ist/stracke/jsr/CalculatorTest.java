package at.tugraz.ist.stracke.jsr;


import org.junit.Test;
import org.junit.Assert;

public class CalculatorTest {

    public CalculatorTest() {
    }

    @Test
    public void add() {
        Calculator calc = new Calculator("henri");
        Assert.assertEquals(10, calc.add(4, 6));
    }

    @Test
    public void add2() {
        Calculator calc = new Calculator("henri");
        Assert.assertEquals(10, calc.add(4, 6));
    }

    @Test
    public void add3() {
        Calculator calc = new Calculator("henri");
        int res = calc.add(4, 6);
        Assert.assertEquals(10, res);
    }

    @Test
    public void addCommutativity() {
        Calculator calc = new Calculator("henri");
        Assert.assertEquals(calc.add(6, 4), calc.add(4, 6));
    }

    @Test
    public void divide() {
        Calculator calc = new Calculator("henri");
        Assert.assertEquals(4, calc.divide(100, 25));
    }

    @Test
    public void divideByZero() {
        Calculator calc = new Calculator("henri");
        Assert.assertEquals(-1, calc.divide(100, 0));
    }

    @Test
    public void divideZeroByNotZero() {
        Calculator calc = new Calculator("henri");
        Assert.assertEquals(0, calc.divide(0, 999));
    }

    @Test
    public void shouldFail() {
        Calculator calc = new Calculator("henri");
        Assert.assertEquals(1, calc.add(0, 0));
    }
}
