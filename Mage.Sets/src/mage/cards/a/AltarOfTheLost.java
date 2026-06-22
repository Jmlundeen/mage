package mage.cards.a;

import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.keyword.FlashbackAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.SpellCastFromZonePredicate;
import mage.filter.predicate.typed.Spell.ability.SpellHasAbilityPredicate;

import java.util.UUID;

/**
 *
 * @author BetaSteward
 */
public final class AltarOfTheLost extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("spells with flashback from a graveyard")
            .addAll(
                    new SpellHasAbilityPredicate(FlashbackAbility.class),
                    SpellCastFromZonePredicate.GRAVEYARD
            );

    public AltarOfTheLost(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        // Altar of the Lost enters the battlefield tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // {T}: Add two mana in any combination of colors. Spend this mana only to cast spells with flashback from a graveyard.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyCombination(2)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add two mana in any combination of colors. Spend this mana only to cast spells with flashback from a graveyard")
                .build()
        );
    }

    private AltarOfTheLost(final AltarOfTheLost card) {
        super(card);
    }

    @Override
    public AltarOfTheLost copy() {
        return new AltarOfTheLost(this);
    }
}
