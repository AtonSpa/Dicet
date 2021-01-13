# <a name="Begin"></a> .onEval

Documentation

- [Expressions](#Expressions)
- [Literals](#Literals)
- [Code](#Code)
- [Operations](#Operations)
- [Functions](#Functions)

## <a name="Expressions"></a> Expressions[&uarr;](#Begin)

An *expression* is a string that contains the expression (duh) to evaluate, with literals and placeholders for variables. It will be evaluated to a result.

Variables, placeholders and results are typed, with 4 available types:

- **STRING**: a string;
- **NUM**: an unbounded, floating point type, backed by `java.math.BigDecimal`;
- **BOOL**: a boolean, **TRUE** or **FALSE** literals;
- **NULL**: NULL/NIL value.

It's possible to evaluate the expression by providing a `Map<String, String>` with the values from the variables in the expression. If the variable is NOT in the `Map` it will be considered as NULL type.

To reference a variable in the expression string, use:

- `$varName` for STRING variables
- `%varName` for NUM variables (cast will be attempted)
- `?varName` for BOOL variables (cast will be attempted)

For example, `%level == 1` will take the variable `level` from the `Map`, cast it to NUM and compare it to the NUM literal `1`, returning a BOOL result of TRUE or FALSE.

## <a name="Literals"></a> Literals[&uarr;](#Begin)

For literals, inside the expression strings:

- STRINGs can have single quotes (`'`) or double quotes (`"`)
- NUMs use a dot as a decimal separator, if needed. E.g. `13.56`.
- BOOLs are limited to (lterally) `true` or `false`.
- NULL is the literal `NULL`

## Code[&uarr;](#Begin)

The code is pretty straightforward. You compile an expression to an `Evaluator` using the static method `compile`:

```java
// Java
final Evaluator expr = Evaluator.compile("%level == 1");
```

The Evaluator object can be reused, cached or put in a `static final` field, it's immutable and thread-safe. Then, you use it to calculate a result:

```java
// Java
final Operand result = expr.evaluate(vars);
```

The `vars` variable is a `java.util.Map<String, String>` with the variables used in the expression string, without the `$`, `%` or `?`:

```java
// Java
final Map<String, String> vars = new HashMap<>();
vars.put("level", "1");
```

The `Operand` object returned by the `evaluate` call has a `getType()` method to extract  its value, and a `getValue()` method to get it. The value is an `Object` of the following Java type:

- if `getType() == ValuedItem.Type.STRING` it will be a `java.lang.String`;
- if `getType() == ValuedItem.Type.NUM` it will be a `java.math.BigDecimal`;
- if `getType() == ValuedItem.Type.BOOL` it will be a `java.lang.Boolean`;
- if `getType() == ValuedItem.Type.NULL` it will be a `null`.

The `BigDecimal`s will always have the minimum scale that is possible without loss of information.

## <a name="Operations"></a> Operations[&uarr;](#Begin)

Operands (variables and literals) can be composed with *operations*, which are quite standard:

Arithmetic operations:

Operation | Effect | Example
---|---|---
`+` | Adds two NUMs | `3 + 4` (=`7`)
`-` | Subtracts two NUMs | `2 - 1` (=`1`)
`*` | Multiplies two NUMs | `2 * 3`  (=`6`)
`/` | Divides two NUMs | `5 / 2` (=`2.5`)

String "Arithmetic" operations:

Operation | Effect | Example
---|---|---
`+` | Concatenates two STRINGs | `"a" + "b"` (=`"ab"`)
`*` | Repeats a STRING | `"a" * 3`  (=`aaa`)

Comparison, they return a Bool:

Operation | Effect | Example
---|---|---
`==` | Equality | `"a" == "a"` (=`true`)
`!=` | Inequality | `"1" != 1` (=`true`)
`>` | Greater than | `4 > 3` (=`true`)
`<` | Less than | `3 < 4` (=`true`)
`>=` | Greater than or equal | `4 >= 4` (=`true`)
`<=` | Less than or equal | `3 <= 3` (=`true`)

Boolean operations, on BOOLs:

Operation | Effect | Example
---|---|---
`&&` | Logic AND | `(1 == 1) && ("a" == "b")` (=`false`)
<code>&#124;&#124;</code> | Logic OR | <code>(1 == 1) &#124;&#124; ("a" != "b")</code> (=`true`)
`~` | Logic NOT | `~("a" != "b")` (=`true`)

You can use `!=` to emulate XOR.

## <a name="Functions"></a> Functions[&uarr;](#Begin)

Many functions are available to manipulate variables and literals. Sometimes they are *overloaded* for different parameter types, but the number of parameters will be the same.

All functions are case insensitive.

TOC|&nbsp;|&nbsp;|&nbsp;|&nbsp;
---|---|---|---|---
Casting, Conditionals|&nbsp;|&nbsp;|&nbsp;|&nbsp;
[`toString`](#toString)|[`toNum`](#toNum)|[`toBool`](#toBool)|[`if`](#if)|[`onNull`](#onNull)
Comparison|&nbsp;|&nbsp;|&nbsp;|&nbsp;
[`max`](#max)|[`min`](#min)|&nbsp;|&nbsp;|&nbsp;
Strings|&nbsp;|&nbsp;|&nbsp;|&nbsp;
[`contains`](#contains)|[`endsWith`](#endsWith)|[`equalsIgnCase`](#equalsIgnCase)|[`indexOf`](#indexOf)|[`left`](#left)
[`len`](#len)|[`lower`](#lower)|[`lPad`](#lPad)|[`lTrim`](#lTrim)|[`replace`](#replace)
[`right`](#right)|[`rPad`](#rPad)|[`rTrim`](#rTrim)|[`startsWith`](#startsWith)|[`substr`](#substr)
[`trim`](#trim)|[`upper`](#upper)|&nbsp;|&nbsp;|&nbsp;
Numeric|&nbsp;|&nbsp;|&nbsp;|&nbsp;
[`abs`](#abs)|[`pow`](#pow)|[`rand`](#rand)|[`rem`](#rem)|[`scale`](#scale)
[`signum`](#signum)|&nbsp;|&nbsp;|&nbsp;|&nbsp;
Date/Time|&nbsp;|&nbsp;|&nbsp;|&nbsp;
[`getEpoch`](#getEpoch)|[`epochToStr`](#epochToStr)|[`strToEpoch`](#strToEpoch)|&nbsp;|&nbsp;

### <a name="toString"></a>toString[&uarr;](#Functions)

Converts a non-STRING parameter to a STRING. If called on a STRING, returns the parameter itself.

What | Type | Meaning
--|--|--
Parameter 1 | NUM | The NUM to convert.
Returns | STRING | The converted STRING, it will have a '.' as a decimal separator, and no trailing zeros after it.

What | Type | Meaning
--|--|--
Parameter 1 | BOOL | The BOOL to convert.
Returns | STRING | The converted STRING, literally `'true'` or `'false'`.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | The NULL to convert.
Returns | STRING | converted STRING with empty value (`""`).

#### Examples

`toString(1) == '1'`<br>
`toString(true) == 'true'`<br>
`toString(NULL) == ''`

### <a name="toNum"></a>toNum[&uarr;](#Functions)

Converts a non-NUM parameter to a NUM. If called on a NUM, returns the parameter itself.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to convert.
Returns | NUM | The parsed NUM, or an `EvalException` if not parseable/valid.

What | Type | Meaning
--|--|--
Parameter 1 | BOOL | The BOOL to convert.
Returns | NUM | `1` if `true`, `0` if `false`.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | The NULL to convert.
Returns | NUM | `0`

#### Examples

`toNum("1") == 1`<br>
`toNum(true) == 1`<br>
`toNum(NULL) == 0`

### <a name="toBool"></a>toBool[&uarr;](#Functions)

Converts a non-BOOL parameter to a BOOL. If called on a BOOL, returns the parameter itself.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to convert.
Returns | NUM | `true` if `"true"` (or any case variation) or `"1"`

What | Type | Meaning
--|--|--
Parameter 1 | NUM | The NUM to convert.
Returns | NUM | `true` if parameter not equal to `0`

What | Type | Meaning
--|--|--
Parameter 1 | NULL | The NULL to convert.
Returns | NUM | `false`

#### Examples

`toBool("1") == true`<br>
`toBool(1.5) == true`<br>
`toBool(NULL) == false`

### <a name="if"></a>if[&uarr;](#Functions)

If the first parameter is `true`, returns the second, otherwise the third. Parameters may be of different types.

What | Type | Meaning
--|--|--
Parameter 1 | BOOL | The condition.
Parameter 2 | *Any* | The second parameter.
Parameter 3 | *Any* | The third parameter.
Returns | *Same as the second or third* | `Parameter 1` if `Parameter 1` is NOT null, else `Parameter 2`.

#### Examples

`if(true, 'a', 'b') == 'a'`<br>
`if(false, '1', 1) == 1`

### <a name="onNull"></a>onNull[&uarr;](#Functions)

If the first parameter is NULL, returns the second; if not, the first. Parameters may be of different types.

What | Type | Meaning
--|--|--
Parameter 1 | *Any* | The first parameter.
Parameter 2 | *Any* | The second parameter.
Returns | *Same as the first or the second* | `Parameter 1` if `Parameter 1` is NOT null, else `Parameter 2`.

#### Examples

`onNull(NULL, NULL) == NULL`<br>
`onNull(NULL, "1") == "1"`<br>
`onNull(1, "1") == 1`

### <a name="max"></a>max[&uarr;](#Functions)

Returns the greater of two parameter of the same type; NULLs are allowed, any value is greater than a NULL.

What | Type | Meaning
--|--|--
Parameter 1 | STRING/NULL | The first STRING to compare.
Parameter 2 | STRING/NULL | The second STRING to compare.
Returns | STRING | The greater of the two, in lexicographical order.

What | Type | Meaning
--|--|--
Parameter 1 | NUM/NULL | The first NUM to compare.
Parameter 2 | NUM/NULL | The second NUM to compare.
Returns | NUM | The greater of the two as numbers.

What | Type | Meaning
--|--|--
Parameter 1 | BOOL/NULL | The first BOOL to compare.
Parameter 2 | BOOL/NULL | The second BOOL to compare.
Returns | BOOL | The greater of the two, `true` > `false`.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | The first param to compare.
Parameter 2 | NULL | The second param to compare.
Returns | NULL | NULL value.

#### Examples

`max("1", "2") == "2"`<br>
`max(1, 2) == 2`<br>
`max(false, true) == true`<br>
`max(NULL, "abcd") == "abcd"`<br>
`max(NULL, -123) == -123`<br>
`max(NULL, false) == false`<br>
`max(NULL, NULL) == NULL`

### <a name="min"></a>min[&uarr;](#Functions)

Returns the smaller of two parameter of the same type; NULLs are allowed, and are smaller than any value.

What | Type | Meaning
--|--|--
Parameter 1 | STRING/NULL | The first STRING to compare.
Parameter 2 | STRING/NULL | The second STRING to compare.
Returns | STRING/NULL | The smaller of the two, in lexicographical order. If one is NULL, then NULL.

What | Type | Meaning
--|--|--
Parameter 1 | NUM/NULL | The first NUM to compare.
Parameter 2 | NUM/NULL | The second NUM to compare.
Returns | NUM/NULL | The smaller of the two as numbers. If one is NULL, then NULL.

What | Type | Meaning
--|--|--
Parameter 1 | BOOL/NULL | The first BOOL to compare.
Parameter 2 | BOOL/NULL | The second BOOL to compare.
Returns | BOOL/NULL | The smaller of the two, `true` > `false`. If one is NULL, then NULL.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | The first param to compare.
Parameter 2 | NULL | The second param to compare.
Returns | NULL | Always NULL.

#### Examples

`min("1", "2") == "1"`<br>
`min(1, 2) == 1`<br>
`min(false, true) == false`<br>
`min(NULL, "abcd") == NULL`<br>
`min(NULL, -123) == NULL`<br>
`min(NULL, false) == NULL`<br>
`min(NULL, NULL) == NULL`

### <a name="contains"></a>contains[&uarr;](#Functions)

Returns `true` if a string contains another string; NULL can be the first parameter, in this case returns `false`.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING in which to search.
Parameter 2 | STRING | The STRING to search.
Returns | BOOL | `true` if the first param contains the second, `false` otherwise.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Parameter 2 | STRING | The STRING to search.
Returns | BOOL | Always `false`.

#### Examples

`contains("haystack", "needle") == false`<br>
`contains("haystack", "sta") == true`<br>
`contains(NULL, "any") == false`

### <a name="endsWith"></a>endsWith[&uarr;](#Functions)

Returns `true` if a string ends with another string; NULL can be the first parameter, in this case returns `false`.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING in which to search.
Parameter 2 | STRING | The STRING to search.
Returns | BOOL | `true` if the first param ends with the second, `false` otherwise.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Parameter 2 | STRING | The STRING to search.
Returns | BOOL | Always `false`.

#### Examples

`endsWith("haystack", "needle") == false`<br>
`endsWith("haystack", "ack") == true`<br>
`endsWith(NULL, "any") == false`

### <a name="equalsIgnCase"></a>equalsIgnCase[&uarr;](#Functions)

Compares two strings for equality, ignoring the case; any parameter can be NULL, in which case `NULL == NULL` holds true.

What | Type | Meaning
--|--|--
Parameter 1 | STRING/NULL | The first STRING to compare.
Parameter 2 | STRING/NULL | The second STRING to compare.
Returns | BOOL | The result of the comparison.

#### Examples

`equalsIgnCase("haystack", "HAYSTACK") == true`<br>
`equalsIgnCase("haystack", "HAYSACK") == false`<br>
`equalsIgnCase("haystack", NULL) == false`<br>
`equalsIgnCase(NULL, NULL) == true`

### <a name="indexOf"></a>indexOf[&uarr;](#Functions)

Returns the position of the first occurrence of specified character(s) in a string.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING in which to search.
Parameter 2 | STRING | The STRING to search.
Returns | NUM | The position, or `-1` if not found.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Parameter 2 | STRING | The STRING to search.
Returns | NUM | Always `-1`.

#### Examples

`indexOf("haystack", "needle") == -1`<br>
`indexOf("haystack", "sta") == 3`<br>
`indexOf(NULL, "any") == -1`

### <a name="left"></a>left[&uarr;](#Functions)

Returns the first *n* chars of a STRING; NULL can be the first parameter, in this case returns NULL.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to extract the chars from.
Parameter 2 | NUM | How many chars to return; must be an integer.
Returns | STRING | The first chars of the first parameter, as many as the second parameter.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Parameter 2 | NUM | How many chars to return; must be an integer.
Returns | NULL | Always NULL.

#### Examples

`left("haystack", 3) == "hay"`<br>
`left(NULL, 3) == NULL`

### <a name="len"></a>len[&uarr;](#Functions)

Returns the length of a STRING; NULL can be the first parameter, in this case returns NULL.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to "measure".
Returns | NUM | The length of the parameter.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Returns | NULL | Always NULL.

#### Examples

`len("my string") == 9`<br>
`len(NULL) == NULL`

### <a name="lower"></a>lower[&uarr;](#Functions)

Returns the lowercase version of a STRING; NULL can be the first parameter, in this case returns NULL.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to process.
Returns | STRING | The lowercase version of the parameter.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Returns | NULL | Always NULL.

#### Examples

`lower("ABC") == "abc"`<br>
`lower(NULL) == NULL`

### <a name="lPad"></a>lPad[&uarr;](#Functions)

Returns a string, left-padded to a specified length with the specified character; or, when the string to be padded is longer than the length specified, the original string itself.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to pad.
Parameter 2 | NUM | The length of the desired string.
Parameter 3 | STRING | The character to pad with.
Returns | STRING | The resulting, padded string.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Parameter 2 | NUM | The length of the desired string.
Parameter 3 | STRING | The character to pad with.
Returns | NULL | Always NULL.

#### Examples

`lPad("hello", 8, 'h') == "hhhhello"`<br>
`lPad("hello", 4, 'h') == "hello"`<br>
`lPad(NULL, 8, 'h') == NULL`<br>

### <a name="lTrim"></a>lTrim[&uarr;](#Functions)

Removes leading whitespace from a STRING.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to trim.
Returns | STRING | The trimmed STRING.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Returns | NULL | Always NULL.

#### Examples

`lTrim(" haystack ") == "haystack "`<br>
`lTrim(NULL) == NULL`

### <a name="replace"></a>replace[&uarr;](#Functions)

Replaces each substring of a string that matches the literal target sequence with the specified literal replacement sequence.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to replace into.
Parameter 2 | STRING | The target string.
Parameter 3 | STRING | The replacement string.
Returns | STRING | The resulting string.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Parameter 2 | STRING | The target string.
Parameter 3 | STRING | The replacement string.
Returns | NULL | Always NULL.

#### Examples

`replace("haystack", "ay", "en") == "henstack"`<br>
`replace("haystack", "a", "e") == "heysteck"`<br>
`replace("haystack", "u", "i") == "haystack"`<br>
`replace(NULL, "ay", "en") == NULL`

### <a name="right"></a>right[&uarr;](#Functions)

Returns the last *n* chars of a STRING; NULL can be the first parameter, in this case returns NULL.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to extract the chars from.
Parameter 2 | NUM | How many chars to return; must be an integer.
Returns | STRING | The last chars of the first parameter, as many as the second parameter.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Parameter 2 | NUM | How many chars to return; must be an integer.
Returns | NULL | Always NULL.

#### Examples

`right("haystack", 3) == "ack"`<br>
`right(NULL, 3) == NULL`

### <a name="rPad"></a>rPad[&uarr;](#Functions)

Returns a string, right-padded to a specified length with the specified character; or, when the string to be padded is longer than the length specified, the original string itself.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to pad.
Parameter 2 | NUM | The length of the desired string.
Parameter 3 | STRING | The character to pad with.
Returns | STRING | The resulting, padded string.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Parameter 2 | NUM | The length of the desired string.
Parameter 3 | STRING | The character to pad with.
Returns | NULL | Always NULL.

#### Examples

`rPad("hello", 8, 'o') == "helloooo"`<br>
`rPad("hello", 4, 'o') == "hello"`<br>
`rPad(NULL, 8, 'o') == NULL`<br>

### <a name="rTrim"></a>rTrim[&uarr;](#Functions)

Removes trailing whitespace from a STRING.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to trim.
Returns | STRING | The trimmed STRING.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Returns | NULL | Always NULL.

#### Examples

`rTrim(" haystack ") == " haystack"`<br>
`rTrim(NULL) == NULL`

### <a name="startsWith"></a>startsWith[&uarr;](#Functions)

Returns `true` if a string starts with another string; NULL can be the first parameter, in this case returns `false`.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING in which to search.
Parameter 2 | STRING | The STRING to search.
Returns | BOOL | `true` if the first param starts with the second, `false` otherwise.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Parameter 2 | STRING | The STRING to search.
Returns | BOOL | Always `false`.

#### Examples

`startsWith("haystack", "needle") == false`<br>
`startsWith("haystack", "hay") == true`<br>
`startsWith(NULL, "any") == false`

### <a name="substr"></a>substr[&uarr;](#Functions)

Returns a substring of a given parameter, specifying start and end. If the parameter is NULL, NULL is returned.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to substring.
Parameter 2 | NUM | The starting index. Must be integer and positive.
Parameter 3 | NUM | The end index. Must be integer and positive.
Returns | STRING | The resulting substring.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Parameter 2 | NUM | The starting index. Must be integer and positive.
Parameter 3 | NUM | The end index. Must be integer and positive.
Returns | NULL | Always NULL.

#### Examples

`substr("haystack", 1, 2) == "a"`<br>
`substr(NULL, 1, 2) == NULL`

### <a name="trim"></a>trim[&uarr;](#Functions)

Removes whitespace from both ends of a STRING.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to trim.
Returns | STRING | The trimmed STRING.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Returns | NULL | Always NULL.

#### Examples

`trim(" haystack ") == "haystack"`<br>
`trim(NULL) == NULL`

### <a name="upper"></a>upper[&uarr;](#Functions)

Returns the uppercase version of a STRING; NULL can be the first parameter, in this case returns NULL.

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The STRING to process.
Returns | STRING | The uppercase version of the parameter.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Returns | NULL | Always NULL.

#### Examples

`upper("abc") == "ABC"`<br>
`upper(NULL) == NULL`

### <a name="abs"></a>abs[&uarr;](#Functions)

Returns the absolute value of a NUM; NULL can be the first parameter, in this case returns NULL.

What | Type | Meaning
--|--|--
Parameter 1 | NUM | The NUM to process.
Returns | NUM | The absolute value of the parameter.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Returns | NULL | Always NULL.

#### Examples

`abs(1) == 1`<br>
`abs(-1) == 1`<br>
`abs(NULL) == NULL`

### <a name="pow"></a>pow[&uarr;](#Functions)

Returns the first NUM elevated to the second NUM-th power; NULL can be the first parameter, in this case returns NULL.

What | Type | Meaning
--|--|--
Parameter 1 | NUM | The NUM to elevate.
Parameter 2 | NUM | The NUM to elevate it for.
Returns | NUM | The result.

What | Type | Meaning
--|--|--
Parameter 1 | NULL | Special case.
Parameter 2 | NUM | The NUM to elevate it for.
Returns | NULL | Always NULL.

#### Examples

`pow(2, 3) == 8`<br>
`pow(NULL, 3) == NULL`

### <a name="rand"></a>rand[&uarr;](#Functions)

Returns a random NUM that falls between the given parameters; it's a floating-point value, use `scale(..., 0, "ROUND")` to coalesce to an integer.

What | Type | Meaning
--|--|--
Parameter 1 | NUM | The min value.
Parameter 2 | NUM | The max value.
Returns | NUM | A random NUM.

#### Examples

`rand(0, 1000) < 1001`

### <a name="rem"></a>rem[&uarr;](#Functions)

Returns the remainder of a division.

What | Type | Meaning
--|--|--
Parameter 1 | NUM | The divisor.
Parameter 2 | NUM | The divident.
Returns | NUM | The remainder of `Parameter 1`/`Parameter 2`.

#### Examples

`rem(14, 3) == 2`<br>
`rem(1.5, 1) == 0.5`

### <a name="scale"></a>scale[&uarr;](#Functions)

Set the scale (number of digits after the decimal separator) of a NUM. If the number is greater than the actual significant digits, it is shortened to that number. 

Rounding mode can be specified as `FLOOR`, `CEIL` or `ROUND`, in which case HALF_EVEN is used (see [docs](https://docs.oracle.com/javase/8/docs/api/java/math/RoundingMode.html#HALF_EVEN)).

If the first argument is NULL, NULL is returned.

What | Type | Meaning
--|--|--
Parameter 1 | NUM | The NUM to process.
Parameter 2 | NUM | The scale. Must be integer.
Parameter 3 | NUM | The rounding mode. `FLOOR`, `CEIL` or `ROUND`.
Returns | NUM | The resulting NUM.

#### Examples

`scale(1.4, 0, 'FLOOR') == 1`<br>
`scale(1.4, 0, 'CEIL') == 2`<br>
`scale(1.4, 0, 'ROUND') == 1`<br>
`scale(1.7, 0, 'ROUND') == 2`

### <a name="signum"></a>signum[&uarr;](#Functions)

Returns -1 if the parameter is smaller than 0, 0 if zero, and otherwise 1.

What | Type | Meaning
--|--|--
Parameter 1 | NUM | The NUM to process.
Returns | NUM | -1 if the parameter is smaller than 0, 0 if zero, and otherwise 1.

#### Examples

`signum(2) == 1`<br>
`signum(-5) == -1`<br>
`signum(0) == 0`<br>
`signum(%a) * abs(%a) = %a`

### <a name="getEpoch"></a>getEpoch[&uarr;](#Functions)

Returns the epoch, the number of seconds from jan 1, 1970. See [here for tooling](https://www.epochconverter.com/).

What | Type | Meaning
--|--|--
Returns | NUM | The epoch.

#### Examples

`getEpoch() > 0`

### <a name="epochToStr"></a>epochToStr[&uarr;](#Functions)

Returns a string with the formatting of an epoch, given the epoch and a format string ([see here for format](https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html)).

What | Type | Meaning
--|--|--
Parameter 1 | NUM | The epoch.
Parameter 2 | STRING | The format string.
Returns | STRING | The formatted date/time string.

#### Examples

`epochToStr(1610319600, "yyyy-MM-dd") == "2021-01-11"`

### <a name="strToEpoch"></a>strToEpoch[&uarr;](#Functions)

Returns the epoch that is the result of the parsing of a date/time string, given the string and a format string ([see here for format](https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html)).

What | Type | Meaning
--|--|--
Parameter 1 | STRING | The date/time string.
Parameter 2 | STRING | The format string.
Returns | NUM | The epoch.

#### Examples

`strToEpoch("2021-01-11", "yyyy-MM-dd") == 1610319600`


