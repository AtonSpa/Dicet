/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 * Copyright 2008 Aton S.p.A. (http://www.aton.eu).
 * Use is subject to license terms.
 */
package com.aton.proj.libs.oneval;

import com.aton.proj.libs.oneval.internals.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

public class Evaluator {
    private static final String LITERAL_PLACEHOLDER = "######";
    private static final int LITERAL_PLACEHOLDER_DIM = LITERAL_PLACEHOLDER
            .length();
    private static final String VARIABLE_PLACEHOLDER = "@#@@#@@#@@@#@@@";
    private static final String NULL_CONST = "NULL";
    private static final String TRUE_CONST = "true";
    private static final String FALSE_CONST = "false";
    private static final char SPACE = ' ';

    public static Evaluator compile(String expression) throws ParseException {
        Evaluator ret = new Evaluator();
        ret.items = parse(expression);
        return ret;
    }

    private Evaluator() {
    }

    private Item[] items;

    private static boolean isNumeric(String string) {
        try {
            Float.parseFloat(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isVariable(String string) {
        return string.charAt(0) == '$' || string.charAt(0) == '%' || string.charAt(0) == '?';
    }

    private static Item getItem(String token, List<String> literals)
            throws ParseException {
        token = token.trim();
        if (token.startsWith(LITERAL_PLACEHOLDER)) { //literal
            int pos = Integer.parseInt(token.substring(LITERAL_PLACEHOLDER_DIM));
            return Operand.strOperand(literals.get(pos));
        }

        if (token.startsWith("%")) //variable
            return new Variable(ValuedItem.Type.NUM, token.substring(1));
        if (token.startsWith("$")) //variable
            return new Variable(ValuedItem.Type.STRING, token.substring(1));
        if (token.startsWith("?")) //variable
            return new Variable(ValuedItem.Type.BOOL, token.substring(1));

        if (NULL_CONST.equals(token))
            return Operand.NULL;
        if (TRUE_CONST.equalsIgnoreCase(token))
            return Operand.TRUE;
        if (FALSE_CONST.equalsIgnoreCase(token))
            return Operand.FALSE;

        Function op = Function.getOperation(token);
        if (op != null)
            return op;

        try {
            BigDecimal bd = new BigDecimal(token);
            return Operand.numOperand(bd);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid numeric value: " + token, 0);
        }

        // throw new ParseException("Unknown literal: " + token, 0);
    }

    // FIXME In this method there are many conversion between String and
    // StringBuilder, and some invocations of String.replace(). This
    // causes the same string to be walked multiple times, and many String
    // objects to be created. This operation is done once per com.aton.proj.libs.oneval.Evaluator
    // lifecycle, so it's not super-serious to fix it, but it's desirable to
    // create a StringBuilder and do the various operations in place, even
    // better (but difficult) in one sweep.
    private static Item[] parse(String expression) throws ParseException {
        // extracts the literal strings (delimited by an unescaped " or ') and
        // puts a placeholder instead of them
        List<String> strLiterals = new ArrayList<>();

        StringBuilder expr = new StringBuilder();
        StringBuilder str = null;
        int position = 0;
        boolean insideLiteral = false, isEscaped = false;
        char quotesUsedForLiteral = ' ';
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (isEscaped) {
                if (insideLiteral)
                    str.append(c);
                else
                    expr.append(c);
                isEscaped = false;
                continue;
            }
            if (c == '\\') {
                isEscaped = true;
                continue;
            }
            if (c == '"' || c == '\'') {
                if (!insideLiteral) {
                    str = new StringBuilder();
                    insideLiteral = true;
                    quotesUsedForLiteral = c;
                    continue;
                }

                if (c == quotesUsedForLiteral) {
                    strLiterals.add(str.toString());
                    expr.append(SPACE);
                    expr.append(LITERAL_PLACEHOLDER);
                    expr.append(position++);
                    expr.append(SPACE);
                    insideLiteral = false;
                    continue;
                }
            }
            if (insideLiteral)
                str.append(c);
            else
                expr.append(c);
        }

        if (insideLiteral)
            throw new ParseException("String literal not properly closed", 0);

        // if an expression ends with a variable, the parsing cycle later on ends
        // without the final evaluation, so it's wrong. Appending a space ensures
        // that it is properly concluded. Dirty-ish, but it works.
        expr.append(' ');
        expression = expr.toString();

        //extract the variables (from a '$' to the next non-literal) and put a
        //placeholder instead of them
        List<String> varLiterals = new ArrayList<>();

        expr = new StringBuilder();
        str = null;
        position = 0;
        insideLiteral = false;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (!insideLiteral && (c == '$')) {
                insideLiteral = true;
                str = new StringBuilder();
            } else if (insideLiteral && (!isAllowedForVarName(c)
                    || i > expression.length() - 1)) {
                insideLiteral = false;
                varLiterals.add(str.toString());
                expr.append(SPACE);
                expr.append(VARIABLE_PLACEHOLDER);
                expr.append(position++);
                expr.append(SPACE);
            }
            if (insideLiteral)
                str.append(c);
            else
                expr.append(c);
        }

        expression = expr.toString();

        //check that brackets are balanced
        int bracketsLevel = 0;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(')
                bracketsLevel++;
            else if (c == ')')
                bracketsLevel--;
        }
        if (bracketsLevel != 0)
            throw new ParseException("Unbalanced brackets", 0);

        //preprocess
        for (Function op : Function.values())
            expression = expression.replace(op.literal, " " + op.literal + " ");

        //replace back the variables
        for (int i = 0; i < varLiterals.size(); i++)
            expression = expression.replace(VARIABLE_PLACEHOLDER + i,
                    varLiterals.get(i));

        StringTokenizer st = new StringTokenizer(expression, " ");
        List<String> tokenized = new ArrayList<>();
        while (st.hasMoreTokens())
            tokenized.add(st.nextToken().trim());
        for (int i = 0; i < tokenized.size(); i++) {
            String current = tokenized.get(i);
            if ("-".equals(current)) {
                boolean isUnaryMinus = false;
                if (i == 0) {
                    isUnaryMinus = true;
                } else {
                    if (isNumeric(tokenized.get(i + 1))) {
                        String prev = tokenized.get(i - 1);

                        isUnaryMinus = !(prev.equals(")") || isNumeric(prev)
                                || isVariable(prev));
                    }
                }
                if (isUnaryMinus) {
                    tokenized.set(i, tokenized.get(i) + tokenized.get(i + 1));
                    tokenized.remove(i + 1);
                }
            } else
                //compensates for '>=', '<=', '!=' ambiguity with '>', '<', '!'
                if (">".equals(current) || "<".equals(current)
                        || "!".equals(current)) {
                    if ((i < tokenized.size() - 1)
                            && "=".equals(tokenized.get(i + 1))) {
                        tokenized.set(i, tokenized.get(i) + tokenized.get(i + 1));
                        tokenized.remove(i + 1);
                    }
                }
        }

        //now we have tokenList, with the tokens, and strLiterals, with the
        //string literals. Some tokens are @#1, @#2, ... and must be substituted
        //with the literals. Next thing is to apply the shunting yard algorithm
        //(based on the 2nd pseudocode listing at www.chris-j.co.uk/parsing.php)
        List<Item> output = new ArrayList<>();
        Stack<Function> stack = new Stack<>();
        for (String token : tokenized) {
            Item item = getItem(token, strLiterals);

            if (item instanceof Operand || item instanceof Variable) {
                output.add(item);
                continue;
            }

            Function op = (Function) item;
            if ((op == Function.COMMA) || (op == Function.CLOSE_BRACKET)) {
                Function cnt;
                while ((cnt = stack.pop()) != Function.OPEN_BRACKET)
                    output.add(cnt);
                if (op == Function.COMMA)
                    stack.push(Function.OPEN_BRACKET);
                continue;
            }

            if (op.type == Function.Type.ZERARY) {
                output.add(op);
                continue;
            }

            if (op.type == Function.Type.UNARY_POSTFIX) {
                output.add(op);
                continue;
            }

            if (op.type == Function.Type.UNARY_PREFIX) {
                stack.push(op);
                continue;
            }

            if (op.type == Function.Type.BINARY) {
                int priority = op.priority;
                if (op.associativity == Function.Associativity.LEFT) {
                    while (!stack.isEmpty()
                            && stack.peek().priority <= priority)
                        output.add(stack.pop());
                } else {
                    while (!stack.isEmpty() && stack.peek().priority < priority)
                        output.add(stack.pop());
                }
                stack.push(op);
            }

            if (op.type == Function.Type.TERNARY) {
                int priority = op.priority;
                if (op.associativity == Function.Associativity.LEFT) {
                    while (!stack.isEmpty()
                            && stack.peek().priority <= priority)
                        output.add(stack.pop());
                } else {
                    while (!stack.isEmpty() && stack.peek().priority < priority)
                        output.add(stack.pop());
                }
                stack.push(op);
            }
        }

        while (!stack.isEmpty())
            output.add(stack.pop());

        // Verify that the operators have the right number of operands. Every
        // operand or variable count as 1, and every operator "consumes" a certain
        // number of operands (2 if binary, ...) while producing one. In the end,
        // exactly one operand should remain.

        int freeOperands = 0;
        for (Item i : output)
            if (i instanceof ValuedItem)
                freeOperands += 1;
            else
                freeOperands -= ((Function) i).type.getOpNum() - 1;

        if (freeOperands != 1)
            throw new ParseException("Wrong number of operands", 0);

        return output.toArray(new Item[]{});
    }

    private static boolean isAllowedForVarName(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9') || (c == '_') || (c == '-');
    }

    private Operand evaluateToOperand(Map<String, String> variables) throws EvalException {
        Stack<Item> stack = new Stack<>();
        for (Item item : items) {
            if (item instanceof Variable) {
                if (variables == null) {
                    stack.push(Operand.NULL);
                    continue;
                }
                Variable var = (Variable) item;
                String value = variables.get(var.getId());
                if (value == null)
                    stack.push(Operand.NULL);
                else if (var.getType() == ValuedItem.Type.STRING)
                    stack.push(Operand.strOperand(value));
                else if (var.getType() == ValuedItem.Type.NUM)
                    stack.push(Operand.numOperand(new BigDecimal(value)));
                else if (var.getType() == ValuedItem.Type.BOOL)
                    stack.push(Operand.boolOperand("1".equals(value) || "true".equalsIgnoreCase(value)));
                else
                    stack.push(Operand.NULL);
            } else if (item instanceof Operand) {
                stack.push(item);
            } else if (item instanceof Function) {
                Function op = (Function) item;
                int opNum = op.type.getOpNum();
                Operand[] operands = new Operand[opNum];
                for (int i = opNum - 1; i >= 0; i--)
                    operands[i] = (Operand) stack.pop();
                Operand res = op.action.perform(operands);
                stack.push(res);
            }
        }

        // This should have been blocked by compilation, but just in case.
        if (stack.size() != 1)
            throw new IllegalArgumentException("Wrong number of operands");

        return ((Operand) stack.pop());
    }

    @SuppressWarnings("unused")
    public Operand evaluate(Map<String, String> variables) throws EvalException {
        return evaluateToOperand(variables);
    }

    @SuppressWarnings("unused")
    public String evaluateToString(Map<String, String> variables) throws EvalException {
        Object ret = evaluate(variables).getValue();
        if (ret == null)
            return null;
        return ret.toString();
    }

    @SuppressWarnings("unused")
    public Operand evaluate() throws EvalException {
        return evaluateToOperand(null);
    }

    @SuppressWarnings("unused")
    public String evaluateToString() throws EvalException {
        return evaluateToString(null);
    }
}
