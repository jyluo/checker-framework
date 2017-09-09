package org.checkerframework.checker.units.parser;

import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.parser.token.UnitsToken;

public class UnitsTokenWalker {
    private List<UnitsToken> tokens;
    private int index = -1;

    public UnitsTokenWalker(@NonNull List<UnitsToken> tokens) {
        this.tokens = tokens;
    }

    public boolean hasNext() {
        return index + 1 < tokens.size();
    }

    public UnitsToken getNext() throws Exception {
        // if (index < tokens.size() - 1) {
        // return tokens.get(++index);
        // } else {
        // throw new Exception("Cannot read past end of token list");
        // }
        UnitsToken token = peekNext();
        index++;
        return token;
    }

    public UnitsToken peekNext() throws Exception {
        if (hasNext()) {
            return tokens.get(index + 1);
        } else {
            throw new Exception("Cannot read past end of token list");
        }
    }

    public boolean isNextOfType(Class<? extends UnitsToken> type) throws Exception {
        // TODO: replace this with ENUM value checks, add getType() to Token interface
        return hasNext() && peekNext().getClass().equals(type);
    }
}
