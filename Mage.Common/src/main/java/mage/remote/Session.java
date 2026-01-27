package mage.remote;

import mage.interfaces.callback.ClientCallback;
import mage.remote.interfaces.*;

/**
 * Network: client/server session
 *
 * @author noxx
 */
public interface Session extends ClientData, Connect, GamePlay, GameTypes, ServerState, ChatSession, Feedback, PlayerActions, Replays, Testable {

    void appendJsonLog(ActionData actionData);

    void setClientReady(boolean ready);

    boolean isClientReady();

    void handleCallback(ClientCallback callback);
}
