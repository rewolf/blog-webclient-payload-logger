package com.github.rewolf.demo.webclientpayloadlogger;

import org.springframework.core.io.buffer.DataBuffer;

public class ByteUtils {
    /**
     * Extracts bytes from the DataBuffer and resets the buffer so that it is ready to be re-read by the regular
     * request sending process.
     *
     * @param data data buffer with encoded data
     * @return copied data as a byte array.
     */
    static byte[] extractBytesAndReset(final DataBuffer data) {
        final byte[] bytes = new byte[data.readableByteCount()];
        data.read(bytes);
        data.readPosition(0);
        return bytes;
    }
}
