package mage.abilities.mana.conditional;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.constants.AbilityType;
import mage.constants.SubType;
import mage.filter.FilterObject;
import mage.game.Game;

import java.util.UUID;

/**
 * Spend this mana only to cast matching spells or activate abilities of matching objects.
 */
public class SpendOrActivateManaCondition extends ManaCondition {

    private final FilterObject<MageObject> filter;
    private final String manaText;
    private final boolean checkChosenCreatureType;
    private final boolean onlyCheckPermanents;

    public SpendOrActivateManaCondition(FilterObject<MageObject> filter, String manaText) {
        this(filter, manaText, false, false);
    }

    public SpendOrActivateManaCondition(String manaText) {
        this(null, manaText, true, false);
    }

    public SpendOrActivateManaCondition(FilterObject<MageObject> filter, String manaText,
                                        boolean checkChosenCreatureType, boolean onlyCheckPermanents) {
        this.filter = filter == null ? null : filter.copy();
        this.manaText = manaText;
        this.checkChosenCreatureType = checkChosenCreatureType;
        this.onlyCheckPermanents = onlyCheckPermanents;
    }

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costToPay) {
        if (game == null || source == null) {
            return false;
        }
        if (!(source.getAbilityType().isActivatedAbility() || source.getAbilityType() == AbilityType.SPELL)) {
            return false;
        }

        MageObject object = game.getObject(source);
        if (object == null) {
            return false;
        }
        if (onlyCheckPermanents && !object.isPermanent(game)) {
            return false;
        }
        if (checkChosenCreatureType) {
            SubType subType = ChooseCreatureTypeEffect.getChosenCreatureType(originalId, game);
            if (subType == null || !object.hasSubtype(subType, game)) {
                return false;
            }
        }
        return filter == null || filter.match(object, game);
    }

    @Override
    public String getManaText() {
        return manaText;
    }
}

