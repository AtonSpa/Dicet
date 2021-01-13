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

public class RPad implements Function.Performable {
    @Override
    public Operand perform(Operand... operands) throws EvalException {
        if (operands.length != 3)
            throw new EvalException("Operands for RPAD must be 3, are " + operands.length);
        Operand o1 = operands[0];
        Operand o2 = operands[1];
        Operand o3 = operands[2];

        if (!(o1.getType() == ValuedItem.Type.STRING || o1.getType() == ValuedItem.Type.NULL)
                || o2.getType() != ValuedItem.Type.NUM
                || o3.getType() != ValuedItem.Type.STRING)
            throw new EvalException("Operands for RPAD must be [String|Null], Num and String");

        if (o1.getType() == ValuedItem.Type.NULL)
            return o1;

        assert o1.getValue() != null;
        String v1 = (String) o1.getValue();

        int v2 = o2.coalesceToInt("Second argument for RPAD must be an integer");
        if (v2 < 0)
            throw new EvalException("Second operand for SUBSTR must be positive");

        if (v2 <= v1.length())
            return o1;

        assert o3.getValue() != null;
        String v3 = (String) o3.getValue();
        if (v3.length() != 1)
            throw new EvalException("Third argument for RPAD must be a single char");

        StringBuilder sb = new StringBuilder(v1);
        while (sb.length() < v2)
            sb.append(v3);
        return Operand.strOperand(sb.toString());
    }
}
