package mage.interfaces.callback;

import com.google.protobuf.ByteString;
import mage.remote.traffic.ZippedObject;
import mage.util.ThreadUtils;
import mage.utils.CompressUtil;
import mage.ws.MessageProto;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

/**
 * Network: server's event to proccess on client side
 *
 * @author BetaSteward_at_googlemail.com
 */
public class ClientCallback implements Serializable {

    // for debug only: simulate bad connection on client side, use launcher's client param like -Dxmage.badConnection
    private static final String SIMULATE_BAD_CONNECTION_PROP = "xmage.badConnection";
    public static final boolean SIMULATE_BAD_CONNECTION;

    static {
        SIMULATE_BAD_CONNECTION = System.getProperty(SIMULATE_BAD_CONNECTION_PROP) != null;
    }

    private UUID objectId;
    private Object data;
    private ClientCallbackMethod method;
    private int messageId;

    public ClientCallback(ClientCallbackMethod method, UUID objectId) {
        this(method, objectId, null);
    }

    public ClientCallback(ClientCallbackMethod method, UUID objectId, Object data) {
        this(method, objectId, data, true);
    }

    public ClientCallback(ClientCallbackMethod method, UUID objectId, Object data, boolean useCompress) {
        this.method = method;
        this.objectId = objectId;
        this.setData(data, useCompress);
    }

    private void simulateBadConnection() {
        if (SIMULATE_BAD_CONNECTION) {
            ThreadUtils.sleep(100);
        }
    }

    public void clear() {
        method = null;
        data = null;
    }

    public UUID getObjectId() {
        return objectId;
    }

    public void setObjectId(UUID objectId) {
        this.objectId = objectId;
    }

    public Object getData() {
        if (this.data instanceof ZippedObject) {
            throw new IllegalStateException("Client data must be decompressed first");
        }
        return data;
    }

    public void setData(Object data, boolean useCompress) {
        if (!useCompress || data == null || data instanceof ZippedObject) {
            this.data = data;
        } else {
            this.data = CompressUtil.compress(data);
            simulateBadConnection();
        }
    }

    public void decompressData() {
        if (this.data instanceof ZippedObject) {
            this.data = CompressUtil.decompress(this.data);
            simulateBadConnection();
        }
    }

    public ClientCallbackMethod getMethod() {
        return method;
    }

    public void setMethod(ClientCallbackMethod method) {
        this.method = method;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getInfo() {
        return String.format("message %d - %s - %s", this.getMessageId(), this.getMethod().getType(), this.getMethod());
    }

    /**
     * Convert this ClientCallback to a protobuf message for WebSocket transmission.
     *
     * @return the protobuf ClientCallback message
     */
    public MessageProto.ClientCallback toProto() {
        MessageProto.ClientCallback.Builder builder = MessageProto.ClientCallback.newBuilder()
                .setMethod(method.name())
                .setMessageId(messageId);

        if (objectId != null) {
            builder.setObjectId(objectId.toString());
        }

        if (data != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(data);
                oos.flush();
                builder.setData(ByteString.copyFrom(baos.toByteArray()));
                builder.setDataType(data.getClass().getName());
            } catch (Exception e) {
                // If serialization fails, just log and skip the data
                // The callback will still have method and objectId
            }
        }

        return builder.build();
    }

    public static ClientCallback fromProto(MessageProto.ClientCallback proto) {
        ClientCallbackMethod method = ClientCallbackMethod.valueOf(proto.getMethod());
        proto.getObjectId();
        UUID objectId = UUID.fromString(proto.getObjectId());
        Object data = null;

        try {
            byte[] dataBytes = proto.getData().toByteArray();
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(dataBytes);
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
            data = ois.readObject();
        } catch (Exception e) {
            // If deserialization fails, just log and keep data as null
        }

        ClientCallback callback = new ClientCallback(method, objectId, data, false);
        callback.setMessageId(proto.getMessageId());
        return callback;
    }
}
