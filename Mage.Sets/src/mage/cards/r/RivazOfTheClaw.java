package mage.cards.r;

import mage.MageInt;
import mage.abilities.common.CastFromGraveyardOnceDuringEachOfYourTurnAbility;
import mage.abilities.common.DiesSourceTriggeredAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.ExileSourceEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.common.FilterCreatureCard;
import mage.filter.common.FilterCreatureSpell;
import mage.filter.predicate.card.CastFromZonePredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author weirddan455
 */
public final class RivazOfTheClaw extends CardImpl {

    private static final FilterTyped manaAbilityFilter = new FilterTyped("Dragon creature spells")
            .addAll(
                    SpellPredicate.instance,
                    IMageObjectPredicate.getOSPPredicate(SubType.DRAGON.getPredicate()),
                    IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate())
            );
    private static final FilterCreatureCard staticAbilityFilter = new FilterCreatureCard("a Dragon creature spell");
    private static final FilterCreatureSpell spellCastFilter = new FilterCreatureSpell("a Dragon creature spell from your graveyard");

    static {
        staticAbilityFilter.add(SubType.DRAGON.getPredicate());
        spellCastFilter.add(SubType.DRAGON.getPredicate());
        spellCastFilter.add(new CastFromZonePredicate(Zone.GRAVEYARD));
    }

    public RivazOfTheClaw(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.LIZARD);
        this.subtype.add(SubType.WARLOCK);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Menace
        this.addAbility(new MenaceAbility(false));

        // {T}: Add two mana in any combination of colors. Spend this mana only to cast Dragon creature spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyCombination(2)
                .condition(new FilteredSpellManaCondition(manaAbilityFilter))
                .ruleText("Add two mana in any combination of colors. Spend this mana only to cast Dragon creature spells.")
                .build()
        );

        // Once during each of your turns, you may cast a Dragon creature spell from your graveyard.
        this.addAbility(new CastFromGraveyardOnceDuringEachOfYourTurnAbility(staticAbilityFilter));

        // Whenever you cast a Dragon creature spell from your graveyard, it gains "When this creature dies, exile it."
        this.addAbility(new SpellCastControllerTriggeredAbility(
                Zone.BATTLEFIELD,
                new GainAbilityTargetEffect(
                        new DiesSourceTriggeredAbility(new ExileSourceEffect().setText("exile it"), false),
                        Duration.Custom,
                        "it gains \"When this creature dies, exile it.\"",
                        true
                ),
                spellCastFilter, false, SetTargetPointer.CARD
        ));
    }

    private RivazOfTheClaw(final RivazOfTheClaw card) {
        super(card);
    }

    @Override
    public RivazOfTheClaw copy() {
        return new RivazOfTheClaw(this);
    }
}
