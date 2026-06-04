package mage.filter.predicate.typed.mageObject.ability;

import mage.MageObject;
import mage.abilities.Abilities;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.keyword.special.JohanVigilanceAbility;
import mage.cards.Card;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.game.Game;
import mage.util.CardUtil;

import java.util.Objects;

public enum NoAbilityPredicate implements IMageObjectPredicate {
    instance;

    // Muraganda Petroglyphs gives a bonus only to creatures that have no rules text at all. This includes true vanilla
    // creatures (such as Grizzly Bears), face-down creatures, many tokens, and creatures that have lost their abilities
    // (due to Ovinize, for example). Any ability of any kind, whether or not the ability functions in the on the
    // battlefield zone, including things like “Cycling {2}” means the creature doesn’t get the bonus.
    // (2007-05-01)
    @Override
    public boolean apply(ObjectSourcePlayer<MageObject> input, Game game) {
        MageObject mageObject = input.getObject();
        boolean isFaceDown = false;
        Abilities<Ability> abilities;
        if (input instanceof Card card) {
            abilities = card.getAbilities(game);
            isFaceDown = card.isFaceDown();
        } else {
            abilities = mageObject.getAbilities();
        }
        if (isFaceDown) {
            // Some Auras and Equipment grant abilities to creatures, meaning the affected creature would no longer
            // get the +2/+2 bonus. For example, Flight grants flying to the enchanted creature. Other Auras and
            // Equipment do not, meaning the affected creature would continue to get the +2/+2 bonus. For example,
            // Dehydration states something now true about the enchanted creature, but doesn’t give it any abilities.
            // Auras and Equipment that grant abilities will use the words “gains” or “has,” and they’ll list a keyword
            // ability or an ability in quotation marks.
            // (2007-05-01)

            for (Ability ability : abilities) {
                // ignore inner face down abilities like turn up and becomes creature
                if (ability.getWorksFaceDown()) {
                    continue;
                }

                // ignore information abilities
                if (CardUtil.isInformationAbility(ability)) {
                    continue;
                }

                if (!Objects.equals(ability.getClass(), SpellAbility.class)
                        && !ability.getClass().equals(JohanVigilanceAbility.class)) {
                    return false;
                }
            }
            return true;
        }
        for (Ability ability : abilities) {
            if (!Objects.equals(ability.getClass(), SpellAbility.class) && !ability.getClass().equals(JohanVigilanceAbility.class)) {
                return false;
            }
        }
        return true;
    }
}
