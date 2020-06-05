/*
 * Copyright (c) 2019, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.grpc;

import com.oracle.coherence.common.base.Logger;
import com.tangosol.io.ByteArrayWriteBuffer;
import com.tangosol.io.Serializer;
import com.tangosol.io.WrapperBufferInput;

import io.grpc.MethodDescriptor;
import io.grpc.Status;

import io.grpc.internal.ReadableBuffer;
import io.grpc.internal.ReadableBuffers;

import java.io.DataInputStream;
import java.io.InputStream;

/**
 * gRPC {@code Marshaller} implementation that delegates to the specified {@link
 * Serializer}.
 *
 * @param <T> the type to marshal
 *
 * @author Aleks Seovic  2017.09.19
 */
public class SerializerMarshaller<T>
        implements MethodDescriptor.Marshaller<T>
    {
    // ---- data members ----------------------------------------------------

    private Serializer serializer;

    private Class<? extends T> clazz;

    // ---- constructors ----------------------------------------------------

    /**
     * Construct {@code SerializerMarshaller} instance.
     *
     * @param serializer the {@link Serializer} to use
     * @param clazz      the class to marshal
     */
    public SerializerMarshaller(Serializer serializer, Class<? extends T> clazz)
        {
        this.serializer = serializer;
        this.clazz = clazz;
        }

    // ---- Marshaller interface --------------------------------------------

    @Override
    public InputStream stream(T value)
        {
        try
            {
            ByteArrayWriteBuffer bufOut = new ByteArrayWriteBuffer(512);
            serializer.serialize(bufOut.getBufferOutput(), value);

            ReadableBuffer bufIn = ReadableBuffers.wrap(bufOut.getRawByteArray(), 0, bufOut.length());
            return ReadableBuffers.openStream(bufIn, true);
            }
        catch (Throwable t)
            {
            Logger.err("Unexpected error during serialization", t);
            throw Status.INTERNAL.withCause(t).asRuntimeException();
            }
        }

    @Override
    public T parse(InputStream stream)
        {
        try
            {
            return serializer.deserialize(new WrapperBufferInput(new DataInputStream(stream)), clazz);
            }
        catch (Throwable t)
            {
            Logger.err("Unexpected error during deserialization", t);
            throw Status.INTERNAL.withCause(t).asRuntimeException();
            }
        }
    }