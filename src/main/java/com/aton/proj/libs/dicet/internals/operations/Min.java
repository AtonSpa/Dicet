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

public class Min implements Function.Performable {
    @Override
    public Operand perform(Operand... operands) throws EvalException {
        if (operands.length != 2)
            throw new EvalException("Operands for MIN must be 2, are " + operands.length);
        Operand o1 = operands[0];
        Operand o2 = operands[1];

        if (o1.getType() != o2.getType() && !(o1.getType() == ValuedItem.Type.NULL || o2.getType() == ValuedItem.Type.NULL))
            throw new EvalException("Operands for MIN must be of the same type or NULL");

        if (o1.getType() == ValuedItem.Type.NULL || o2.getType() == ValuedItem.Type.NULL)
            return Operand.NULL;

        switch (o1.getType()) {
            case STRING: {
                int comparison = ((String) o1.getValue()).compareTo((String) o2.getValue());
                if (comparison > 0)
                    return o2;
                return o1;
            }
            case NUM: {
                int comparison = ((BigDecimal) o1.getValue()).compareTo((BigDecimal) o2.getValue());
                if (comparison > 0)
                    return o2;
                return o1;
            }
            case BOOL:
                return o1 != o2 ? Operand.FALSE : o1;
        }

        throw new EvalException("Invalid parameters combination for MIN");
    }
}
