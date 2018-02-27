package com.cherkovskiy.comprehensive_serializer.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

//TODO:
public interface SerializerService {

    <T extends Serializable> T deserializeFrom(InputStream from, Class<T> token);

    <T extends Serializable> void serializeTo(T object, OutputStream to) throws IOException, ClassNotFoundException;
}
