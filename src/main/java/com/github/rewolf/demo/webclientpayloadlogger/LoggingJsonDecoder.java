package com.github.rewolf.demo.webclientpayloadlogger;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A Wrapper around the default Jackson2JsonDecoder that captures the payload before deserialization and supplies it to a consumer
 *
 * @author rewolf
 */
@RequiredArgsConstructor
public class LoggingJsonDecoder extends Jackson2JsonDecoder {
    private final Consumer<byte[]> payloadConsumer;

    @Override
    public Mono<Object> decodeToMono(final Publisher<DataBuffer> input, final ResolvableType elementType, final MimeType mimeType, final Map<String, Object> hints) {
        // Buffer for bytes from each published DataBuffer
        final ByteArrayOutputStream payload = new ByteArrayOutputStream();

        // Augment the Flux, and intercept each group of bytes buffered
        final Flux<DataBuffer> interceptor = Flux.from(input)
                                                 .doOnNext(buffer -> bufferBytes(payload, buffer))
                                                 .doOnComplete(() -> payloadConsumer.accept(payload.toByteArray()));

        // Return the original method, giving our augmented Publisher
        return super.decodeToMono(interceptor, elementType, mimeType, hints);
    }

    private void bufferBytes(final ByteArrayOutputStream bao, final DataBuffer buffer) {
        try {
            bao.write(ByteUtils.extractBytesAndReset(buffer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
