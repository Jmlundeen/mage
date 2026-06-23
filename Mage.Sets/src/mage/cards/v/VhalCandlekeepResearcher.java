package mage.cards.v;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.common.ChooseABackgroundAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.SourcePermanentToughnessValue;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.ManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.stack.Spell;

import java.util.UUID;

public final class VhalCandlekeepResearcher extends CardImpl {

    private static final VhalCandlekeepResearcherManaCondition manaCondition = new VhalCandlekeepResearcherManaCondition();

    public VhalCandlekeepResearcher(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // {T}: Add an amount of {C} equal to Vhal, Candlekeep Researcher's toughness. This mana can't be spent to cast spells from your hand.
        this.addAbility(new ComposedManaAbilityBuilder()
                .addDynamic(SourcePermanentToughnessValue.instance, ManaType.COLORLESS)
                .condition(manaCondition)
                .cost(new TapSourceCost())
                .zone(Zone.BATTLEFIELD)
                .ruleText("Add an amount of {C} equal to {this}'s toughness. This mana can't be spent to cast spells from your hand.")
                .build()
        );

        // Choose a Background
        this.addAbility(ChooseABackgroundAbility.getInstance());
    }

    private VhalCandlekeepResearcher(final VhalCandlekeepResearcher card) {
        super(card);
    }

    @Override
    public VhalCandlekeepResearcher copy() {
        return new VhalCandlekeepResearcher(this);
    }
}

class VhalCandlekeepResearcherManaCondition extends ManaCondition {

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costToPay) {
        if (!(source instanceof SpellAbility)) {
            return true;
        }
        MageObject object = game.getObject(source);
        if (!source.isControlledBy(game.getOwnerId(object))) {
            return false;
        }
        if (object instanceof Spell) {
            return ((Spell) object).getFromZone() != Zone.HAND;
        }
        // checking mana without real cast
        return game.inCheckPlayableState() && game.getState().getZone(source.getSourceId()) != Zone.HAND;
    }

    @Override
    public String getManaText() {
        return "This mana can't be spent to cast spells from your hand";
    }
}
