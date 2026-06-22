
package mage.cards.m;

import mage.MageInt;
import mage.abilities.ActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.TopLibraryCardTypeCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.common.continuous.PlayWithTheTopCardRevealedEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;

import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class MulDayaChannelers extends CardImpl {

    private static final String rule1 = "As long as the top card of your library is a creature card, {this} gets +3/+3";

    public MulDayaChannelers(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}{G}");
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.DRUID);
        this.subtype.add(SubType.SHAMAN);

        this.color.setGreen(true);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Play with the top card of your library revealed.
        this.addAbility(new SimpleStaticAbility(new PlayWithTheTopCardRevealedEffect()));

        // As long as the top card of your library is a creature card, Mul Daya Channelers gets +3/+3.
        ContinuousEffect boostEffect = new GenericContinuousEffect(Duration.WhileOnBattlefield, Outcome.BoostCreature)
                .withAddPower(3)
                .withAddToughness(3);
        ConditionalContinuousEffect effect = new ConditionalContinuousEffect(boostEffect, new TopLibraryCardTypeCondition(CardType.CREATURE), rule1);
        this.addAbility(new SimpleStaticAbility(effect));

        // As long as the top card of your library is a land card, Mul Daya Channelers has "T: Add two mana of any one color."
        ActivatedAbility manaAbility = new AnyColorManaAbility(2);
        ContinuousEffect gainAbilityEffect = new GenericContinuousEffect(Duration.WhileOnBattlefield, Outcome.AddAbility)
                .withGainedAbilities(manaAbility);
        effect = new ConditionalContinuousEffect(gainAbilityEffect,
                new TopLibraryCardTypeCondition(CardType.LAND),
                "As long as the top card of your library is a land card, {this} has \"{T}: Add two mana of any one color.\"");
        this.addAbility(new SimpleStaticAbility(effect));

    }

    private MulDayaChannelers(final MulDayaChannelers card) {
        super(card);
    }

    @Override
    public MulDayaChannelers copy() {
        return new MulDayaChannelers(this);
    }
}
