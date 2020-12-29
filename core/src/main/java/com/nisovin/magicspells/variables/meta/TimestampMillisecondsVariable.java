package com.nisovin.magicspells.variables.meta;

import java.time.Instant;

import com.nisovin.magicspells.variables.variabletypes.MetaVariable;

public class TimestampMillisecondsVariable extends MetaVariable {

    @Override
    public double getValue(String player) {
        Instant instant = Instant.now();
        return instant.toEpochMilli();
    }

}
