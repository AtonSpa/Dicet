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

import java.util.regex.Pattern;

public class RTrim implements Function.Performable {
    private static final Pattern RTRIM = Pattern.compile("\\s+$");

    @Override
    public Operand perform(Operand... operands) throws EvalException {
        if (operands.length != 1)
            throw new EvalException("Operands for RTRIM must be 1, are " + operands.length);
        Operand o1 = operands[0];

        if (o1.getType() == ValuedItem.Type.NULL)
            return o1;

        assert o1.getValue() != null;

        if (o1.getType() == ValuedItem.Type.STRING) {
            String before = (String) o1.getValue();
            String after = RTRIM.matcher(before).replaceAll("");
            return Operand.strOperand(after);
        }

        throw new EvalException("Operand for RTRIM must be [String|Null]");
    }
}
