
package mage.constants;

import mage.ws.v1.view.ViewProto;

/**
 *
 * @author LevelX2
 */

public enum SkillLevel {
    BEGINNER ("Beginner"),
    CASUAL("Casual"),
    SERIOUS ("Serious");

    private final String text;

    SkillLevel(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public ViewProto.SkillLevel toProto() {
        return switch (this) {
            case BEGINNER -> ViewProto.SkillLevel.BEGINNER;
            case CASUAL -> ViewProto.SkillLevel.CASUAL;
            case SERIOUS -> ViewProto.SkillLevel.SERIOUS;
        };
    }

    public static SkillLevel fromProto(ViewProto.SkillLevel proto) {
        return switch (proto) {
            case BEGINNER -> BEGINNER;
            case SERIOUS -> SERIOUS;
            default -> CASUAL;
        };
    }

}