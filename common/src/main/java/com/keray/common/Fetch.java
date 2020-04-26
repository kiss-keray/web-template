package com.keray.common;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author keray
 * @date 2019/05/16 17:14
 */
public class Fetch<T> {
    private T data;
    private Exception e;
    private Catch<T> tCatch;

    public Fetch(T data) {
        this.data = data;
    }

    public Fetch(T data, Exception e) {
        this.data = data;
        this.e = e;
    }

    public static <T> Fetch<T> fetch(T data) {
        return new Fetch<>(data);
    }

    public static <T> Fetch<T> fetch(T data, Exception e) {
        return new Fetch<>(data, e);
    }

    public Fetch<T> then(Consumer<T> then) {
        try {
            then.accept(data);
        } catch (Exception e) {
            this.e = e;
        }
        catchAccept();
        return this;
    }

    public <S> Fetch<S> then(Function<T, S> then) {
        try {
            return fetch(then.apply(data));
        } catch (Exception e) {
            this.e = e;
        }
        catchAccept();
        return (Fetch<S>) this;
    }

    public Fetch<T> catchFetch(Catch<T> tCatch) {
        this.tCatch = tCatch;
        catchAccept();
        return this;
    }


    public void finallyFetch(Consumer<T> then) {
        try {
            then.accept(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void catchAccept() {
        if (tCatch != null && e != null) {
            T d = tCatch.accept(e, data);
            if (d != null) {
                this.data = d;
            }
            this.e = null;
        }
    }

    public interface Catch<T> {
        T accept(Exception e,T t);
    }

    public T getData() {
        if (e != null) {
            if (tCatch != null) {
                catchAccept();
            } else {
                throw new RuntimeException(e);
            }
        }
        return data;
    }
}

