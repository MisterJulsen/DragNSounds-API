package de.mrjulsen.dragnsounds.core.data;

import java.util.Arrays;

public enum ECompareOperation {
    EQUALS((byte)0),
    LESS_THAN((byte)1),
    LARGER_THAN((byte)2),
    NOT((byte)3),
    CONTAINS((byte)4),
    CONTAINS_NOT((byte)5),
    STARTS_WITH((byte)6),
    ENDS_WITH((byte)7);

    private byte id;

    private ECompareOperation(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static ECompareOperation getById(byte id) {
        return Arrays.stream(values()).filter(x -> x.getId() == id).findFirst().orElse(EQUALS);
    }

    public <T extends Comparable<T>> boolean compare(T a, T b) {
        switch (this) {
            case EQUALS:
                return a.equals(b);
            case LARGER_THAN:
                return a.compareTo(b) > 0;
            case LESS_THAN:
                return a.compareTo(b) < 0;
            case NOT:
                return !a.equals(b);
            case CONTAINS:
                return a.toString().contains(b.toString());
            case CONTAINS_NOT:
                return !a.toString().contains(b.toString());                
            case STARTS_WITH:
                return a.toString().startsWith(b.toString());
            case ENDS_WITH:
                return !a.toString().endsWith(b.toString());
            default:
                return false;
        }
    }
}