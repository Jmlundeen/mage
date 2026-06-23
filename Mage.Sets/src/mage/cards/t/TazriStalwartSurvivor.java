package mage.cards.t;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.mana.ComposedManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.ManaCondition;
import mage.abilities.mana.providers.common.manaType.SourceManaTypes;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class TazriStalwartSurvivor extends CardImpl {

    public static final String ruleText = "Add one mana of any of this creature's colors. "
            + "Spend this mana only to activate an ability of a creature. "
            + "Activate only if this creature has another activated ability.";

    public TazriStalwartSurvivor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Each creature you control has "{T}: Add one mana of any of this creature's colors. Spend this mana only to activate an ability of a creature. Activate only if this creature has another activated ability."
        ComposedManaAbility manaAbility = new ComposedManaAbilityBuilder()
                .addDynamicChoice(StaticValue.get(1), SourceManaTypes.instance)
                .condition(TazriStalwartSurvivorManaCondition.instance)
                .cost(new TapSourceCost())
                .activationCondition(TazriStalwartSurvivorActivationCondition.instance)
                .ruleText(ruleText)
                .build();
        this.addAbility(new SimpleStaticAbility(new GainAbilityControlledEffect(
                manaAbility,
                Duration.WhileOnBattlefield,
                StaticFilters.FILTER_PERMANENT_CREATURE)
                .setText(String.format("each creature you control has \"{T}: %s\"", ruleText)))
        );

        // {W}{U}{B}{R}{G}, {T}: Mill five cards. Put all creature cards with activated abilities that aren't mana abilities from among the milled cards into your hand.
        Ability ability = new SimpleActivatedAbility(
                new TazriStalwartSurvivorMillEffect(), new ManaCostsImpl<>("{W}{U}{B}{R}{G}")
        );
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private TazriStalwartSurvivor(final TazriStalwartSurvivor card) {
        super(card);
    }

    @Override
    public TazriStalwartSurvivor copy() {
        return new TazriStalwartSurvivor(this);
    }
}

enum TazriStalwartSurvivorActivationCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        return permanent != null
                && permanent
                .getAbilities(game)
                .stream()
                .filter(Ability::isActivatedAbility)
                .map(Ability::getOriginalId)
                .anyMatch(abilityId -> !source.getOriginalId().equals(abilityId));
    }
}

class TazriStalwartSurvivorManaCondition extends ManaCondition {
    static final TazriStalwartSurvivorManaCondition instance = new TazriStalwartSurvivorManaCondition();

    @Override
    public boolean apply(Game game, Ability source) {
        if (!source.isActivatedAbility()) {
            return false;
        }
        MageObject object = game.getObject(source);
        return object != null && object.isCreature(game) && !source.isActivated();
    }

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costsToPay) {
        return apply(game, source);
    }

    @Override
    public String getManaText() {
        return "Spend this mana only to activate an ability of a creature";
    }
}

class TazriStalwartSurvivorMillEffect extends OneShotEffect {

    TazriStalwartSurvivorMillEffect() {
        super(Outcome.Benefit);
        staticText = "mill five cards. Put all creature cards with activated abilities " +
                "that aren't mana abilities from among the milled cards into your hand";
    }

    private TazriStalwartSurvivorMillEffect(final TazriStalwartSurvivorMillEffect effect) {
        super(effect);
    }

    @Override
    public TazriStalwartSurvivorMillEffect copy() {
        return new TazriStalwartSurvivorMillEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        Cards cards = player.millCards(5, source, game);
        cards.removeIf(uuid -> !game.getCard(uuid).isCreature(game));
        cards.removeIf(uuid -> game
                .getCard(uuid)
                .getAbilities(game)
                .stream()
                .noneMatch(Ability::isNonManaActivatedAbility));
        player.moveCards(cards, Zone.HAND, source, game);
        return true;
    }
}
