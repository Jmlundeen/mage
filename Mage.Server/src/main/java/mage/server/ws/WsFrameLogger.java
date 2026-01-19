package mage.server.ws;

import org.apache.log4j.Logger;

/**
 * Logs protobuf frame sizes greater than a hard limit to help catch oversized payloads.
 *
 * Do not log payload contents.
 */
public final class WsFrameLogger {

    private static final int LOG_THRESHOLD_BYTES = 50 * 1024;

    private WsFrameLogger() {
    }

    public static void logIfLarge(Logger logger, String direction, String sessionId, String payloadType, int sizeBytes) {
        if (sizeBytes <= LOG_THRESHOLD_BYTES) {
            return;
        }

        double sizeKb = sizeBytes / 1024.0;
        logger.warn(String.format("WS protobuf frame > 50KB: dir=%s sessionId=%s payload=%s size=%.1fKB", direction, sessionId, payloadType, sizeKb));
    }
}
