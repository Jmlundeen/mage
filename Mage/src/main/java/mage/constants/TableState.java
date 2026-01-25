package mage.constants;

import mage.ws.v1.view.ViewProto;

/**
 *
 * @author North
 */
public enum TableState {
    WAITING ("Waiting for players"),
    READY_TO_START("Waiting to start"),
    STARTING ("Starting"),
    DRAFTING ("Drafting"),
    CONSTRUCTING ("Constructing"),
    DUELING ("Dueling"),
    SIDEBOARDING ("Sideboarding"),   
    FINISHED ("Finished");

    private final String text;

    TableState(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public ViewProto.TableState toProto() {
        return switch (this) {
            case WAITING -> ViewProto.TableState.WAITING;
            case READY_TO_START -> ViewProto.TableState.READY_TO_START;
            case STARTING -> ViewProto.TableState.STARTING;
            case DRAFTING -> ViewProto.TableState.DRAFTING;
            case CONSTRUCTING -> ViewProto.TableState.CONSTRUCTING;
            case DUELING -> ViewProto.TableState.DUELING;
            case SIDEBOARDING -> ViewProto.TableState.SIDEBOARDING;
            case FINISHED -> ViewProto.TableState.FINISHED;
        };
    }

    public static TableState fromProto(ViewProto.TableState proto) {
        return switch (proto) {
            case WAITING -> WAITING;
            case READY_TO_START -> READY_TO_START;
            case STARTING -> STARTING;
            case DRAFTING -> DRAFTING;
            case CONSTRUCTING -> CONSTRUCTING;
            case DUELING -> DUELING;
            case SIDEBOARDING -> SIDEBOARDING;
            case FINISHED -> FINISHED;
            default -> throw new IllegalArgumentException("Unknown TableState proto: " + proto);
        };
    }
}
