package mage.players.net;

import mage.ws.v1.model.ModelProto;

import java.io.Serializable;

/**
 * @author LevelX2
 */
public class UserSkipPrioritySteps implements Serializable {

    final SkipPrioritySteps yourTurn;
    final SkipPrioritySteps opponentTurn;

    boolean stopOnDeclareAttackers = true;
    boolean stopOnDeclareBlockersWithZeroPermanents = false;
    boolean stopOnDeclareBlockersWithAnyPermanents = true;
    boolean stopOnAllMainPhases = true;
    boolean stopOnAllEndPhases = true;
    boolean stopOnStackNewObjects = true;

    public UserSkipPrioritySteps() {
        yourTurn = new SkipPrioritySteps();
        opponentTurn = new SkipPrioritySteps();
    }

    public SkipPrioritySteps getYourTurn() {
        return yourTurn;
    }

    public SkipPrioritySteps getOpponentTurn() {
        return opponentTurn;
    }

    public boolean isStopOnDeclareBlockersWithZeroPermanents() {
        return stopOnDeclareBlockersWithZeroPermanents;
    }

    public void setStopOnDeclareBlockersWithZeroPermanents(boolean stopOnDeclareBlockersWithZeroPermanents) {
        this.stopOnDeclareBlockersWithZeroPermanents = stopOnDeclareBlockersWithZeroPermanents;
    }

    public boolean isStopOnDeclareAttackers() {
        return stopOnDeclareAttackers;
    }

    public void setStopOnDeclareAttackersDuringSkipActions(boolean stopOnDeclareAttackersDuringSkipActions) {
        this.stopOnDeclareAttackers = stopOnDeclareAttackersDuringSkipActions;
    }

    public boolean isStopOnDeclareBlockersWithAnyPermanents() {
        return stopOnDeclareBlockersWithAnyPermanents;
    }

    public void setStopOnDeclareBlockersWithAnyPermanents(boolean stopOnDeclareBlockersWithAnyPermanents) {
        this.stopOnDeclareBlockersWithAnyPermanents = stopOnDeclareBlockersWithAnyPermanents;
    }

    public boolean isStopOnAllMainPhases() {
        return stopOnAllMainPhases;
    }

    public void setStopOnAllMainPhases(boolean stopOnAllMainPhases) {
        this.stopOnAllMainPhases = stopOnAllMainPhases;
    }

    public boolean isStopOnAllEndPhases() {
        return stopOnAllEndPhases;
    }

    public void setStopOnAllEndPhases(boolean stopOnAllEndPhases) {
        this.stopOnAllEndPhases = stopOnAllEndPhases;
    }

    public boolean isStopOnStackNewObjects() {
        return stopOnStackNewObjects;
    }

    public void setStopOnStackNewObjects(boolean stopOnStackNewObjects) {
        this.stopOnStackNewObjects = stopOnStackNewObjects;
    }

    public ModelProto.UserSkipPrioritySteps toProto() {
        return ModelProto.UserSkipPrioritySteps.newBuilder()
                .setYourTurn(yourTurn.toProto())
                .setOpponentTurn(opponentTurn.toProto())
                .setStopOnDeclareAttackers(stopOnDeclareAttackers)
                .setStopOnDeclareBlockersWithZeroPermanents(stopOnDeclareBlockersWithZeroPermanents)
                .setStopOnDeclareBlockersWithAnyPermanents(stopOnDeclareBlockersWithAnyPermanents)
                .setStopOnAllMainPhases(stopOnAllMainPhases)
                .setStopOnAllEndPhases(stopOnAllEndPhases)
                .setStopOnStackNewObjects(stopOnStackNewObjects)
                .build();
    }

    public static UserSkipPrioritySteps fromProto(ModelProto.UserSkipPrioritySteps proto) {
        UserSkipPrioritySteps steps = new UserSkipPrioritySteps();

        // Copy yourTurn settings
        SkipPrioritySteps yourTurnFromProto = SkipPrioritySteps.fromProto(proto.getYourTurn());
        steps.yourTurn.setUpkeep(yourTurnFromProto.isUpkeep());
        steps.yourTurn.setDraw(yourTurnFromProto.isDraw());
        steps.yourTurn.setMain1(yourTurnFromProto.isMain1());
        steps.yourTurn.setBeforeCombat(yourTurnFromProto.isBeforeCombat());
        steps.yourTurn.setEndOfCombat(yourTurnFromProto.isEndOfCombat());
        steps.yourTurn.setMain2(yourTurnFromProto.isMain2());
        steps.yourTurn.setEndOfTurn(yourTurnFromProto.isEndOfTurn());

        // Copy opponentTurn settings
        SkipPrioritySteps opponentTurnFromProto = SkipPrioritySteps.fromProto(proto.getOpponentTurn());
        steps.opponentTurn.setUpkeep(opponentTurnFromProto.isUpkeep());
        steps.opponentTurn.setDraw(opponentTurnFromProto.isDraw());
        steps.opponentTurn.setMain1(opponentTurnFromProto.isMain1());
        steps.opponentTurn.setBeforeCombat(opponentTurnFromProto.isBeforeCombat());
        steps.opponentTurn.setEndOfCombat(opponentTurnFromProto.isEndOfCombat());
        steps.opponentTurn.setMain2(opponentTurnFromProto.isMain2());
        steps.opponentTurn.setEndOfTurn(opponentTurnFromProto.isEndOfTurn());

        // Copy other settings
        steps.setStopOnDeclareAttackersDuringSkipActions(proto.getStopOnDeclareAttackers());
        steps.setStopOnDeclareBlockersWithZeroPermanents(proto.getStopOnDeclareBlockersWithZeroPermanents());
        steps.setStopOnDeclareBlockersWithAnyPermanents(proto.getStopOnDeclareBlockersWithAnyPermanents());
        steps.setStopOnAllMainPhases(proto.getStopOnAllMainPhases());
        steps.setStopOnAllEndPhases(proto.getStopOnAllEndPhases());
        steps.setStopOnStackNewObjects(proto.getStopOnStackNewObjects());

        return steps;
    }
}
