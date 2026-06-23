package mage.cards.t;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.TurnFaceUpAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.ManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.game.Game;
import mage.game.stack.Spell;

import java.util.UUID;

/**
 *
 * @author DominionSpy
 */
public final class TinStreetGossip extends CardImpl {

    public TinStreetGossip(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}{G}");

        this.subtype.add(SubType.LIZARD);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // {T}: Add {R}{G}. Spend this mana only to cast face-down spells or to turn creatures face up.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(0, 0, 0, 1, 1, 0, 0)
                .condition(new TinStreetGossipManaCondition())
                .ruleText("Add {R}{G}. Spend this mana only to cast face-down spells or to turn creatures face up")
                .build()
        );
    }

    private TinStreetGossip(final TinStreetGossip card) {
        super(card);
    }

    @Override
    public TinStreetGossip copy() {
        return new TinStreetGossip(this);
    }
}

class TinStreetGossipManaCondition extends ManaCondition {

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costToPay) {
        MageObject object = game.getObject(source);
        if (object instanceof Spell) {
            return object.isFaceDown();
        }
        return source instanceof TurnFaceUpAbility;
    }
}
