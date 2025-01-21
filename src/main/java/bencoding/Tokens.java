package bencoding;

public enum Tokens {
    TOKEN_INTEGER('i'),
    TOKEN_LIST('l'),
    TOKEN_DICT('d'),
    TOKEN_END('e'),
    TOKEN_STRING_SEPARATOR(':');

    private Character identifier;

    Tokens(Character identifier) {
        this.identifier = identifier;
    }

    public Character getIdentifier() {
        return identifier;
    }
}
