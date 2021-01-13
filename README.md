# .onEval 0.2.0

**.onEval** is a Java library that evaluates expressions. It's not a full-fledged programming language, but a fast way to calculate the result of an expression string, given a set of variables. It's for exaple useful to add a simple language to filter results, or to implement quick scripting in many occasions. 

The string to be evaluated is something like `$a == "foo" + "bar"`; it may or may not contain a variable. There is a simple type system for variable and literals, and the variables can be supplied to the engine as a Map. In the case above, the result will be a Boolean value, and the variable is typed as a String (by the `$`).

Expressions can also be pre-compiled to speed up the evaluation.

The library is small, [very fast](https://github.com/AtonSpa/onEval/wiki/Benchmarks), and doesn't have external dependencies other than JVM (8+). There are 35+ built-in functions, and it's quite easy to hack it to add more.

[**Documentation is in the Wiki.**](https://github.com/AtonSpa/onEval/wiki)

Example of usage:

```java
final Map<String, String> vars = new HashMap<>();
vars.put("myString", "Hello, World 1!");

// extracts the 13th char from the variable, converts it to a number
// and adds 1
final String expressionString = "toNum(substr($myString, 13, 14)) + 1";

// this can be put in a constant, it's also thread safe
final Evaluator expr = Evaluator.compile(expressionString);

// calculate the result
final Operand result = expr.evaluate(vars);

System.out.println(result.getType()); // NUM
System.out.println(result.getValue()); // 2
```

Of course, it can be used from any JVM-based language:

```kotlin
// same example, in kotlin

val vars = mapOf("myString", "Hello, World 1!")
val expressionString = "toNum(substr($myString, 13, 14)) + 1"
val expr = Evaluator.compile(expressionString)
val result = expr.evaluate(vars)

println(result.type) // NUM
println(result.value) // 2
```

This library is derived from a work made by [Aton S.p.A](https://www.aton.com), since 2008.

(c) 2008-, [Aton S.p.A.](https://www.aton.com) under [CDDL v1](https://opensource.org/licenses/CDDL-1.0)