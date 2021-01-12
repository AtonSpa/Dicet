import com.aton.proj.libs.dicet.Evaluator;
import com.aton.proj.libs.dicet.internals.EvalException;
import com.aton.proj.libs.dicet.internals.Operand;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestEvaluator {
    private final Map<String, String> variables = new HashMap<>();

    @BeforeAll
    public void setUp() {
        variables.put("a", "0");
        variables.put("b", "1");
        variables.put("c", "2");
        variables.put("d", "d");
        variables.put("f", null);
        variables.put("g", "0.5");
        variables.put("h", "h");
        variables.put("i", "string");
        variables.put("j", "str");
        variables.put("k", "rin");
        variables.put("l", "ng");
        variables.put("m", "-1");
    }

    private Operand eval(String expression) throws ParseException, EvalException {
        Evaluator eval = Evaluator.compile(expression);
        return eval.evaluate(variables);
    }

    @Test
    public void testTrivial() throws ParseException, EvalException {
        assertEquals(eval("1"), Operand.numOperand(BigDecimal.ONE));
        testTruth("1 == 1");
        testTruth("(%a == %b) || ((%a + %b) == 1)");
        testFalseness("(%a == %b) && ((%a + %b) == 1)");
    }

    @Test
    public void testNull() throws ParseException, EvalException {
        testTruth("NULL == NULL");
        testTruth("$e == NULL");
        testTruth("$f == NULL");
        testTruth("$e == $f");
    }

    @Test
    public void testNumeric() throws ParseException, EvalException {
        testTruth("%a == 0");
        testTruth("%a+%b == 1");
        testTruth("%b+%b == %c");
        testTruth("%b/%c == %g");
        testTruth("%g+%g == %b");
        testTruth("%g*4 ==  %c");
        testTruth("(%a == %b) || ((%a + %b) == 1)");
        testFalseness("(%a == %b) && ((%a + %b) == 1)");
        testTruth("%m < 0");
        testTruth("%m -1 == -2");
        testTruth("%m - 1 == -2");
        testTruth("1-%m == 2");
        testTruth("1- %m == 2");
        testTruth("1 -%m == 2");
        testTruth("1 - %m == 2");

        testTruth("pow(2,3)==8");
    }

    @Test
    public void testStrings() throws ParseException, EvalException {
        testTruth("$a == \"0\"");
        testTruth("($a+$d) == \"0d\"");
        testTruth("($h+$d) == \"hd\"");
        testTruth("$j+\"i\"+$l == $i");

        testTruth("toString(%g*2) == $b");
        testTruth("toString(%g+%g)+\"\" == $b+\"\"");
        testTruth("trim(\" \"+toString(%g+%g)) == $b+\"\"");
    }

    @Test
    public void testStringComplex() throws ParseException, EvalException {
        // see bug 161
        testTruth("'a' + 1 == 'a' + '1'");

        // notice that 0.5 + 0.5 = 1, not 1.0
        testTruth("'a' + (0.5 + 0.5) == 'a'+'1'");

        testTruth("%g+%g + 'a' == '1a'"); //($g+$g)+"a"
        testTruth("\"a\" + $g + %g == \"a0.50.5\""); //("a"+$g)+$g
        testTruth("\"\"+(%g+%g) == toString(1)"); //(""+$g)+$g = $g+$g = 0.5+0.5 = 1.0

        testTruth("$d + (%g + %g) == $d + $b");
    }

    @Test
    public void testStringFuncs() throws ParseException, EvalException {
        testTruth("startsWith(\"hd\", $h)");
        testTruth("endsWith(\"hd\", $d)");
        testTruth("startsWith($i, $j)");
        testTruth("endsWith($i, $l)");
        testTruth("contains($i, $j)");
        testTruth("contains($i, $k)");
        testTruth("contains($i, $l)");
        testFalseness("endsWith($i, $j)");
        testFalseness("startsWith($i, $l)");
        testTruth("left($i, 3) == $j");
        testTruth("right($i, 2) == $l");
        testTruth("right(left($i, 5), 3) == $k");
        testEquals("trim(' ' + $i + ' ')", "$i");
        testEquals("len($i)", "6");
        testEquals("upper($i)", "'STRING'");
        testEquals("lower($i)", "'string'");
        testTruth("left($i, 3) + right(left($i, 4), 1) + right($i, 2) == $i");
    }

    @Test
    public void testAssociativity() throws ParseException, EvalException {
        // see bug XXX
        testTruth("(NULL == NULL) || startsWith(\"xyz\", \"xy\")");
        testTruth("NULL == NULL || startsWith(\"xyz\", \"xy\")");

        testTruth("2 + 3 == 10 - 5");
        assertEquals(eval("toNum(2 + toNum(3 == 3) + 2 == 5) - 2 * 0.5"), eval("0"));
    }

    @Test
    public void testBoolean() throws ParseException, EvalException {
        testTruth("~false");
        testFalseness("false");

        testTruth("true");
        testFalseness("~true");

        testFalseness("false && true");
        testTruth("true && true");

        testFalseness("false || false");
        testTruth("true || false");

        testTruth("~false && true");
        testFalseness("~true && true");

        testTruth("~false || false");
        testFalseness("~true || false");

        testTruth("~?a || ?a");
        testFalseness("~?b || ?a");
    }

    @Test
    public void testNumFuncs() throws ParseException, EvalException {
        testEquals("scale(1.4, 0, 'FLOOR')", "1");
        testEquals("scale(1.4, 0, 'CEIL')", "2");
        testEquals("scale(1.4, 0, 'ROUND')", "1");
        testEquals("scale(1.7, 0, 'ROUND')", "2");
    }

    @Test
    public void testDateFuncs() throws ParseException, EvalException {
        testTruth("0 + getEpoch() > 1000");
        testTruth("0 - GETEPOCH() < -1000");
        testTruth("strToEpoch('2021-01-01', 'yyyy-MM-dd') < getEpoch()");
        testTruth("strToEpoch(epochToStr(getEpoch() - 1000, 'yyyy-MM-dd'), 'yyyy-MM-dd') < getEpoch()");
    }

    @Test
    public void testRandom() throws ParseException, EvalException {
        testTruth("rand(1, 1000) <= 1000");
        testTruth("rand(1, 1000) > 0");
    }

    @Test
    // tests for documentation examples
    public void testDocs() throws ParseException, EvalException {
        testTruth("toString(1) == '1'");
        testTruth("toString(true) == 'true'");
        testTruth("toString(NULL) == ''");
        testTruth("toNum(\"1\") == 1");
        testTruth("toNum(true) == 1");
        testTruth("toNum(NULL) == 0");
        testTruth("toBool(\"1\") == true");
        testTruth("toBool(1.5) == true");
        testTruth("toBool(NULL) == false");
        testTruth("max(\"1\", \"2\") == \"2\"");
        testTruth("max(1, 2) == 2");
        testTruth("max(false, true) == true");
        testTruth("max(NULL, \"abcd\") == \"abcd\"");
        testTruth("max(NULL, -123) == -123");
        testTruth("max(NULL, false) == false");
        testTruth("max(NULL, NULL) == NULL");
        testTruth("min(\"1\", \"2\") == \"1\"");
        testTruth("min(1, 2) == 1");
        testTruth("min(false, true) == false");
        testTruth("min(NULL, \"abcd\") == NULL");
        testTruth("min(NULL, -123) == NULL");
        testTruth("min(NULL, false) == NULL");
        testTruth("min(NULL, NULL) == NULL");
        testTruth("contains(\"haystack\", \"needle\") == false");
        testTruth("contains(\"haystack\", \"sta\") == true");
        testTruth("contains(NULL, \"any\") == false");
        testTruth("endsWith(\"haystack\", \"needle\") == false");
        testTruth("endsWith(\"haystack\", \"ack\") == true");
        testTruth("endsWith(NULL, \"any\") == false");
        testTruth("left(\"haystack\", 3) == \"hay\"");
        testTruth("left(NULL, 3) == NULL");
        testTruth("len(\"my string\") == 9");
        testTruth("len(NULL) == NULL");
        testTruth("lower(\"ABC\") == \"abc\"");
        testTruth("lower(NULL) == NULL");
        testTruth("right(\"haystack\", 3) == \"ack\"");
        testTruth("right(NULL, 3) == NULL");
        testTruth("startsWith(\"haystack\", \"needle\") == false");
        testTruth("startsWith(\"haystack\", \"hay\") == true");
        testTruth("startsWith(NULL, \"any\") == false");
        testTruth("substr(\"haystack\", 1, 2) == \"a\"");
        testTruth("substr(NULL, 1, 2) == NULL");
        testTruth("trim(\" haystack \") == \"haystack\"");
        testTruth("trim(NULL) == NULL");
        testTruth("upper(\"abc\") == \"ABC\"");
        testTruth("upper(NULL) == NULL");
        testTruth("abs(1) == 1");
        testTruth("abs(-1) == 1");
        testTruth("abs(NULL) == NULL");
        testTruth("pow(2, 3) == 8");
        testTruth("pow(NULL, 3) == NULL");
        testTruth("rand(0, 1000) < 1001");
        testTruth("rem(14, 3) == 2");
        testTruth("rem(1.5, 1) == 0.5");
        testTruth("scale(1.4, 0, 'FLOOR') == 1");
        testTruth("scale(1.4, 0, 'CEIL') == 2");
        testTruth("scale(1.4, 0, 'ROUND') == 1");
        testTruth("scale(1.7, 0, 'ROUND') == 2");
        testTruth("signum(2) == 1");
        testTruth("signum(-5) == -1");
        testTruth("signum(0) == 0");
        testTruth("getEpoch() > 0");
        testTruth("epochToStr(1610319600, \"yyyy-MM-dd\") == \"2021-01-11\"");
        testTruth("strToEpoch(\"2021-01-11\", \"yyyy-MM-dd\") == 1610319600");
        testTruth("onNull(NULL, NULL) == NULL");
        testTruth("onNull(NULL, \"1\") == \"1\"");
        testTruth("onNull(1, \"1\") == 1");
        testTruth("if(true, 'a', 'b') == 'a'");
        testTruth("if(false, '1', 1) == 1");
        testTruth("rTrim(\" haystack \") == \" haystack\"");
        testTruth("rTrim(NULL) == NULL");
        testTruth("rPad(\"hello\", 8, 'o') == \"helloooo\"");
        testTruth("rPad(\"hello\", 4, 'o') == \"hello\"");
        testTruth("rPad(NULL, 8, 'o') == NULL");
        testTruth("replace(\"haystack\", \"ay\", \"en\") == \"henstack\"");
        testTruth("replace(\"haystack\", \"a\", \"e\") == \"heysteck\"");
        testTruth("replace(\"haystack\", \"u\", \"i\") == \"haystack\"");
        testTruth("replace(NULL, \"ay\", \"en\") == NULL");
        testTruth("lTrim(\" haystack \") == \"haystack \"");
        testTruth("lTrim(NULL) == NULL");
        testTruth("lPad(\"hello\", 8, 'h') == \"hhhhello\"");
        testTruth("lPad(\"hello\", 4, 'h') == \"hello\"");
        testTruth("lPad(NULL, 8, 'h') == NULL");
        testTruth("indexOf(\"haystack\", \"needle\") == -1");
        testTruth("indexOf(\"haystack\", \"sta\") == 3");
        testTruth("indexOf(NULL, \"any\") == -1");
        testTruth("equalsIgnCase(\"haystack\", \"HAYSTACK\") == true");
        testTruth("equalsIgnCase(\"haystack\", \"HAYSACK\") == false");
        testTruth("equalsIgnCase(\"haystack\", NULL) == false");
        testTruth("equalsIgnCase(NULL, NULL) == true");
    }

    @Test
    public void testWrongNumOfOperands() {
        testFailCompilation("(5*2)8");
    }

    private void testEquals(String e1, String e2) throws ParseException, EvalException {
        Evaluator ev1 = Evaluator.compile(e1);
        Evaluator ev2 = Evaluator.compile(e2);
        assertEquals(ev1.evaluate(variables), ev2.evaluate(variables));
    }

    private void testTruth(String expression) throws ParseException, EvalException {
        Evaluator eval = Evaluator.compile(expression);
        assertEquals(eval.evaluate(variables), Operand.TRUE);
    }

    private void testFalseness(String expression) throws ParseException, EvalException {
        Evaluator eval = Evaluator.compile(expression);
        assertEquals(eval.evaluate(variables), Operand.FALSE);
    }

    private void testFailCompilation(String expression) {
        try {
            Evaluator.compile(expression);
            fail();
        } catch (Exception e) {
        }
    }
}
