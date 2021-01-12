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

public class Not implements Function.Performable {
    @Override
    public Operand perform(Operand... operands) throws EvalException {
        if (operands.length != 1)
            throw new EvalException("Operands for NOT must be 1, are " + operands.length);
        Operand o1 = operands[0];

        if (o1.getType() != ValuedItem.Type.BOOL)
            throw new EvalException("Operand for NOT must be Boolean");

        assert o1.getValue() != null;
        return Operand.boolOperand(!((Boolean) o1.getValue()).booleanValue());
    }
}
