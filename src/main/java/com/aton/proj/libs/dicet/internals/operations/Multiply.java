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

public class Multiply implements Function.Performable {
    @Override
    public Operand perform(Operand... operands) throws EvalException {
        if (operands.length != 2)
            throw new EvalException("Operands for MULTIPLY must be 2, are " + operands.length);
        Operand o1 = operands[0];
        Operand o2 = operands[1];

        switch (o1.getType()) {
            case STRING:
                assert o1.getValue() != null;
                if (o2.getType() == ValuedItem.Type.NUM) {
                    String v1 = (String) o1.getValue();
                    int v2 = o2.coalesceToInt("It's only possible to MULTIPLY a string with an integer");
                    return Operand.strOperand(v1.repeat(Math.max(0, v2)));
                } else
                    throw new EvalException("It's only possible to MULTIPLY a string with an integer");
            case NUM:
                switch (o2.getType()) {
                    case STRING:
                        throw new EvalException("Cannot MULTIPLY on strings");
                    case NUM:
                        assert o1.getValue() != null;
                        assert o2.getValue() != null;
                        return Operand.numOperand(((BigDecimal) o1.getValue()).multiply((BigDecimal) o2.getValue()));
                    case BOOL:
                        throw new EvalException("Cannot MULTIPLY on booleans");
                    case NULL:
                        throw new EvalException("Cannot MULTIPLY on nulls");
                }
            case BOOL:
                throw new EvalException("Cannot MULTIPLY on booleans");
            case NULL:
                throw new EvalException("Cannot MULTIPLY on nulls");
        }

        throw new EvalException("Invalid parameters combination for MULTIPLY");
    }
}
