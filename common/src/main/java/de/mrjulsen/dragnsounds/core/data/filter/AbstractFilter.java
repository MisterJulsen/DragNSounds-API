package de.mrjulsen.dragnsounds.core.data.filter;

import de.mrjulsen.dragnsounds.core.data.ECompareOperation;

public abstract class AbstractFilter<T> implements IFilter<T> {

    protected final String key, value;
    protected final ECompareOperation operation;

    public AbstractFilter(String key, String value, ECompareOperation operation) {
        this.key = key;
        this.value = value;
        this.operation = operation;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String value() {
        return value;
    }
    
    @Override
    public ECompareOperation compareOperation() {
        return operation;
    }
}
