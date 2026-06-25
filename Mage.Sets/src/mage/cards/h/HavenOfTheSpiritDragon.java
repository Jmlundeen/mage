package mage.cards.h;

import mage.ConditionalMana;
import mage.MageObject;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.SacrificeSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.CreatureCastManaCondition;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterCard;
import mage.filter.FilterTyped;
import mage.filter.predicate.Predicate;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.game.Game;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class HavenOfTheSpiritDragon extends CardImpl {

    private static final FilterCard filter = new FilterCard("Dragon creature card or Ugin planeswalker card from your graveyard");
    private static final FilterTyped manaFilter = new FilterTyped("Dragon")
            .addAll(
                    IMageObjectPredicate.getOSPPredicate(SubType.DRAGON.getPredicate()),
                    SpellPredicate.instance
            );

    static {
        filter.add(Predicates.or(new DragonCreatureCardPredicate(),
                new UginPlaneswalkerCardPredicate()));
    }

    public HavenOfTheSpiritDragon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: add one mana of any color. Spend this mana only to cast a Dragon creature spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(manaFilter))
                .ruleText("Add one mana of any color. Spend this mana only to cast a Dragon creature spell.")
                .build()
        );

        // {2}, {T}, Sacrifice Haven of the Spirit Dragon: Return target Dragon creature card or Ugin planeswalker card from your graveyard to your hand.
        Ability ability = new SimpleActivatedAbility(new ReturnFromGraveyardToHandTargetEffect(), new ManaCostsImpl<>("{2}"));
        ability.addCost(new TapSourceCost());
        ability.addCost(new SacrificeSourceCost());
        ability.addTarget(new TargetCardInYourGraveyard(filter));
        this.addAbility(ability);

    }

    private HavenOfTheSpiritDragon(final HavenOfTheSpiritDragon card) {
        super(card);
    }

    @Override
    public HavenOfTheSpiritDragon copy() {
        return new HavenOfTheSpiritDragon(this);
    }
}

class HavenOfTheSpiritConditionalMana extends ConditionalMana {

    HavenOfTheSpiritConditionalMana(Mana mana) {
        super(mana);
        staticText = "Spend this mana only to cast a Dragon creature spell.";
        addCondition(new HavenOfTheSpiritManaCondition());
    }
}

class HavenOfTheSpiritManaCondition extends CreatureCastManaCondition {

    @Override
    public boolean apply(Game game, Ability source, UUID manaProducer, Cost costToPay) {
        if (super.apply(game, source)) {
            MageObject object = game.getObject(source);
            if (object != null && object.hasSubtype(SubType.DRAGON, game)
                    && object.isCreature(game)) {
                return true;
            }
        }
        return false;
    }
}

class DragonCreatureCardPredicate implements Predicate<Card> {

    public DragonCreatureCardPredicate() {
    }

    @Override
    public boolean apply(Card input, Game game) {
        return input.isCreature(game)
                && input.hasSubtype(SubType.DRAGON, game);
    }

    @Override
    public String toString() {
        return "";
    }
}

class UginPlaneswalkerCardPredicate implements Predicate<Card> {

    public UginPlaneswalkerCardPredicate() {
    }

    @Override
    public boolean apply(Card input, Game game) {
        return input.isPlaneswalker(game)
                && input.hasSubtype(SubType.UGIN, game);
    }

    @Override
    public String toString() {
        return "";
    }
}
