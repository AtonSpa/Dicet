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
package com.aton.proj.libs.oneval.internals.operations;

import com.aton.proj.libs.oneval.internals.EvalException;
import com.aton.proj.libs.oneval.internals.Function;
import com.aton.proj.libs.oneval.internals.Operand;
import com.aton.proj.libs.oneval.internals.ValuedItem;

import java.math.BigDecimal;
import java.util.Random;

public class Rand implements Function.Performable {
    private static final Random RND = new Random();

    @Override
    public Operand perform(Operand... operands) throws EvalException {
        if (operands.length != 2)
            throw new EvalException("Operands for RAND must be 2, are " + operands.length);
        Operand o1 = operands[0];
        Operand o2 = operands[1];

        if (o1.getType() != ValuedItem.Type.NUM || o2.getType() != ValuedItem.Type.NUM)
            throw new EvalException("Operands for RAND must be Num");

        assert o1.getValue() != null;
        double v1 = ((BigDecimal) o1.getValue()).doubleValue();
        assert o2.getValue() != null;
        double v2 = ((BigDecimal) o2.getValue()).doubleValue();

        if (Double.compare(v1, v2) >= 0)
            throw new EvalException("Operand 1 must be less than operand 2 for RAND");

        double ret;
        synchronized (RND) {
            ret = (RND.nextDouble() % (v2 - v1)) + v1;
        }
        return Operand.numOperand(new BigDecimal(ret));
    }
}
