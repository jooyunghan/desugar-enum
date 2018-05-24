package com.company;

import java.util.function.Function;

@FunctionalInterface
interface ExFunction<A, B> {
    static <A, B> Function<A, B> shh(ExFunction<A, B> f) {
        return a -> {
            try {
                return f.call(a);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    B call(A a) throws Exception;
}
