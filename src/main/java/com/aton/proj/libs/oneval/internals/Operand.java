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
package com.aton.proj.libs.oneval.internals;

import java.math.BigDecimal;
import java.util.Objects;

public final class Operand extends ValuedItem {
    private Operand(Type type, String strValue, BigDecimal numValue, Boolean boolValue) {
        super(type);
        this.strValue = strValue;
        this.numValue = numValue;
        this.boolValue = boolValue;
    }

    public static final Operand NULL = new Operand(Type.NULL, null, null, null);
    public static final Operand TRUE = new Operand(Type.BOOL, null, null, Boolean.TRUE);
    public static final Operand FALSE = new Operand(Type.BOOL, null, null, Boolean.FALSE);

    public static Operand strOperand(String str) {
        if (str == null)
            throw new IllegalArgumentException("NULL argument for string operand");
        return new Operand(Type.STRING, str, null, null);
    }

    public static Operand numOperand(BigDecimal num) {
        if (num == null)
            throw new IllegalArgumentException("NULL argument for string operand");
        return new Operand(Type.NUM, null, num.stripTrailingZeros(), null);
    }

    public static Operand boolOperand(boolean val) {
        return val ? TRUE : FALSE;
    }

    private final String strValue;
    private final BigDecimal numValue;
    private final Boolean boolValue;

    public Object getValue() {
        switch (getType()) {
            case STRING:
                return strValue;
            case NUM:
                return numValue;
            case BOOL:
                return boolValue;
            default:
                return null;
        }
    }

    public int coalesceToInt(String err) throws EvalException {
        if (getType() != Type.NUM)
            throw new EvalException(err);
        try {
            return numValue.intValueExact();
        } catch (ArithmeticException e) {
            throw new EvalException(err);
        }
    }

    public long coalesceToLong(String err) throws EvalException {
        if (getType() != Type.NUM)
            throw new EvalException(err);
        try {
            return numValue.longValueExact();
        } catch (ArithmeticException e) {
            throw new EvalException(err);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operand operand = (Operand) o;
        return Objects.equals(strValue, operand.strValue) && Objects.equals(getValue(), operand.getValue());
    }

    @Override
    public String toString() {
        return "Operand{" +
                "Type=" + getType() + ", " +
                "Value=" + getValue() +
                '}';
    }
}
