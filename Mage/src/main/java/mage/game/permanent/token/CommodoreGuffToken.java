package mage.game.permanent.token;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

/**
 * @author TheElk801
 */
public final class CommodoreGuffToken extends TokenImpl {

    private static final FilterTyped filter = new FilterTyped("a planeswalker spell")
            .addAll(SpellPredicate.instance, IMageObjectPredicate.getOSPPredicate(CardType.PLANESWALKER.getPredicate()));

    public CommodoreGuffToken() {
        super("Wizard Token", "1/1 red Wizard creature token with \"{T}: Add {R}. Spend this mana only to cast a planeswalker spell.\"");
        cardType.add(CardType.CREATURE);
        subtype.add(SubType.WIZARD);
        color.setRed(true);
        power = new MageInt(1);
        toughness = new MageInt(1);

        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.RedMana(1))
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("{T}: Add {R}. Spend this mana only to cast a planeswalker spell.")
                .build()
        );
    }

    private CommodoreGuffToken(final CommodoreGuffToken token) {
        super(token);
    }

    public CommodoreGuffToken copy() {
        return new CommodoreGuffToken(this);
    }
}
