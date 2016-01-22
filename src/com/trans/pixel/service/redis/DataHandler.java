package com.trans.pixel.service.redis;

public interface DataHandler<T> {
	T doInRedis(T data);
}
