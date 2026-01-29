
package mage.players.net;

import mage.constants.PhaseStep;
import mage.ws.v1.model.ModelProto;

import java.io.Serializable;

/**
 *
 * @author LevelX2
 */
public class SkipPrioritySteps implements Serializable {

    boolean upkeep = false;
    boolean draw = false;
    boolean main1 = true;
    boolean beforeCombat = false;
    boolean endOfCombat = false;
    boolean main2 = true;
    boolean endOfTurn = false;

    public boolean isUpkeep() {
        return upkeep;
    }

    public void setUpkeep(boolean upkeep) {
        this.upkeep = upkeep;
    }

    public boolean isDraw() {
        return draw;
    }

    public void setDraw(boolean draw) {
        this.draw = draw;
    }

    public boolean isMain1() {
        return main1;
    }

    public void setMain1(boolean main1) {
        this.main1 = main1;
    }

    public boolean isBeforeCombat() {
        return beforeCombat;
    }

    public void setBeforeCombat(boolean beforeCombat) {
        this.beforeCombat = beforeCombat;
    }

    public boolean isEndOfCombat() {
        return endOfCombat;
    }

    public void setEndOfCombat(boolean endOfCombat) {
        this.endOfCombat = endOfCombat;
    }

    public boolean isMain2() {
        return main2;
    }

    public void setMain2(boolean main2) {
        this.main2 = main2;
    }

    public boolean isEndOfTurn() {
        return endOfTurn;
    }

    public void setEndOfTurn(boolean endOfTurn) {
        this.endOfTurn = endOfTurn;
    }

    public boolean isPhaseStepSet(PhaseStep phaseStep) {
        return switch (phaseStep) {
            case UPKEEP -> isUpkeep();
            case DRAW -> isDraw();
            case PRECOMBAT_MAIN -> isMain1();
            case BEGIN_COMBAT -> isBeforeCombat();
            case END_COMBAT -> isEndOfCombat();
            case POSTCOMBAT_MAIN -> isMain2();
            case END_TURN -> isEndOfTurn();
            default -> true;
        };
    }

    public ModelProto.SkipPrioritySteps toProto() {
        return ModelProto.SkipPrioritySteps.newBuilder()
                .setUpkeep(upkeep)
                .setDraw(draw)
                .setMain1(main1)
                .setBeforeCombat(beforeCombat)
                .setEndOfCombat(endOfCombat)
                .setMain2(main2)
                .setEndOfTurn(endOfTurn)
                .build();
    }

    public static SkipPrioritySteps fromProto(ModelProto.SkipPrioritySteps proto) {
        SkipPrioritySteps steps = new SkipPrioritySteps();
        steps.setUpkeep(proto.getUpkeep());
        steps.setDraw(proto.getDraw());
        steps.setMain1(proto.getMain1());
        steps.setBeforeCombat(proto.getBeforeCombat());
        steps.setEndOfCombat(proto.getEndOfCombat());
        steps.setMain2(proto.getMain2());
        steps.setEndOfTurn(proto.getEndOfTurn());
        return steps;
    }

}
