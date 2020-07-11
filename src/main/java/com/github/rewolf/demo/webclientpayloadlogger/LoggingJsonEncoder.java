package com.github.rewolf.demo.webclientpayloadlogger;


import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A Wrapper around the default Jackson2JsonEncoder that captures the serialized body and supplies it to a consumer
 *
 * @author rewolf
 */
@RequiredArgsConstructor
public class LoggingJsonEncoder extends Jackson2JsonEncoder {
    private final Consumer<byte[]> payloadConsumer;

    @Override
    public DataBuffer encodeValue(final Object value, final DataBufferFactory bufferFactory,
                                  final ResolvableType valueType, @Nullable final MimeType mimeType, @Nullable final Map<String, Object> hints) {

        // Encode/Serialize data to JSON
        final DataBuffer data = super.encodeValue(value, bufferFactory, valueType, mimeType, hints);

        // Interception: Generate Signature and inject header into request
        payloadConsumer.accept(ByteUtils.extractBytesAndReset(data));

        // Return the data as normal
        return data;
    }

    @Override
    public boolean canEncode(final ResolvableType elementType, final MimeType mimeType) {
        return super.canEncode(elementType, mimeType);
    }
}
