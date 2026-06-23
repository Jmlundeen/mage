package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.Effect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.game.Game;
import mage.players.Player;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author spjspj
 */
public final class CorruptedGrafstone extends CardImpl {

    public CorruptedGrafstone(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        // Corrupted Grafstone enters the battlefield tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // {T}: Choose a color of a card in your graveyard. Add one mana of that color.
        this.addAbility(new ComposedManaAbilityBuilder()
                .addDynamicChoice(StaticValue.get(1), CorruptedGrafstoneManaTypes.instance)
                .cost(new TapSourceCost())
                .ruleText("Choose a color of a card in your graveyard. Add one mana of that color")
                .build()
        );
    }

    private CorruptedGrafstone(final CorruptedGrafstone card) {
        super(card);
    }

    @Override
    public CorruptedGrafstone copy() {
        return new CorruptedGrafstone(this);
    }
}

enum CorruptedGrafstoneManaTypes implements ManaTypeProvider {
    instance;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        Set<ManaType> manaTypes = EnumSet.noneOf(ManaType.class);
        if (game == null || source == null || source.getControllerId() == null) {
            return manaTypes;
        }
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return manaTypes;
        }
        for (Card card : controller.getGraveyard().getCards(game)) {
            manaTypes.addAll(ManaType.getManaTypesFromObjectColor(card.getColor(game)));
            if (manaTypes.size() == 5) {
                break;
            }
        }
        return manaTypes;
    }
}
