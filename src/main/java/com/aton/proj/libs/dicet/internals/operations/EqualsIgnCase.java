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

public class EqualsIgnCase implements Function.Performable {
    @Override
    public Operand perform(Operand... operands) throws EvalException {
        if (operands.length != 2)
            throw new EvalException("Operands for EQUALSIGNCASE must be 2, are " + operands.length);
        Operand o1 = operands[0];
        Operand o2 = operands[1];

        if (!(o1.getType() == ValuedItem.Type.STRING || o1.getType() == ValuedItem.Type.NULL) ||
                !(o2.getType() == ValuedItem.Type.STRING || o2.getType() == ValuedItem.Type.NULL))
            throw new EvalException("Operands for EQUALSIGNCASE must be [String|Null] and [String|Null]");

        if (o1.getType() == ValuedItem.Type.NULL && o2.getType() == ValuedItem.Type.NULL)
            return Operand.TRUE;

        if (o1.getType() == ValuedItem.Type.NULL || o2.getType() == ValuedItem.Type.NULL)
            return Operand.FALSE;

        assert o1.getValue() != null;
        String v1 = (String) o1.getValue();
        assert o2.getValue() != null;
        String v2 = (String) o2.getValue();

        return Operand.boolOperand((v1).equalsIgnoreCase(v2));
    }
}
