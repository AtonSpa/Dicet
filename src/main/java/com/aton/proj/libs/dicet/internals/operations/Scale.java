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
package com.aton.proj.libs.dicet.internals.operations;

import com.aton.proj.libs.dicet.internals.EvalException;
import com.aton.proj.libs.dicet.internals.Function;
import com.aton.proj.libs.dicet.internals.Operand;
import com.aton.proj.libs.dicet.internals.ValuedItem;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Scale implements Function.Performable {
    @Override
    public Operand perform(Operand... operands) throws EvalException {
        if (operands.length != 3)
            throw new EvalException("Operands for SCALE must be 3, are " + operands.length);
        Operand o1 = operands[0];
        Operand o2 = operands[1];
        Operand o3 = operands[2];

        if ((o1.getType() != ValuedItem.Type.NUM && o1.getType() != ValuedItem.Type.NULL)
                || o2.getType() != ValuedItem.Type.NUM
                || o3.getType() != ValuedItem.Type.STRING)
            throw new EvalException("Operands for SCALE must be [Num|NULL], Num and String");

        if (o1.getType() == ValuedItem.Type.NULL)
            return o1;

        BigDecimal v1 = ((BigDecimal) o1.getValue());
        int v2 = o2.coalesceToInt("Second operand for SCALE must be an integer");

        if (v2 < 0)
            throw new EvalException("Operand 2 must be positive");

        String v3 = (String) o3.getValue();

        if (!v3.equalsIgnoreCase("FLOOR") &&
                !v3.equalsIgnoreCase("CEIL") &&
                !v3.equalsIgnoreCase("ROUND"))
            throw new EvalException("Third operand for SCALE must be [\"FLOOR\"|\"CEIL\",|\"ROUND\"]");

        if (v3.equalsIgnoreCase("FLOOR"))
            return Operand.numOperand(v1.setScale(v2, RoundingMode.FLOOR));
        if (v3.equalsIgnoreCase("CEIL"))
            return Operand.numOperand(v1.setScale(v2, RoundingMode.CEILING));
        return Operand.numOperand(v1.setScale(v2, RoundingMode.HALF_EVEN));
    }
}
