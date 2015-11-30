package com.trans.pixel.utils;

import java.io.InputStream;

public interface HTTPBodyResolver<T> {
    public T solve(InputStream in);
}
