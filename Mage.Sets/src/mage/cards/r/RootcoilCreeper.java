package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.ExileSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ReturnToHandTargetEffect;
import mage.abilities.keyword.FlashbackAbility;
import mage.abilities.mana.AnyColorManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.filter.FilterCard;
import mage.filter.FilterTyped;
import mage.filter.common.FilterOwnedCard;
import mage.filter.predicate.mageobject.AbilityPredicate;
import mage.filter.predicate.typed.Spell.SpellCastFromZonePredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.target.common.TargetCardInExile;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class RootcoilCreeper extends CardImpl {

    private static final FilterCard filter = new FilterOwnedCard("card with flashback you own from exile");
    private static final FilterTyped manaFilter = new FilterTyped("spell from your graveyard")
            .addAll(
                    SpellPredicate.instance,
                    SpellCastFromZonePredicate.GRAVEYARD,
                    TargetController.YOU.getOwnerPredicate()
            );

    static {
        filter.add(new AbilityPredicate(FlashbackAbility.class));
    }

    public RootcoilCreeper(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}{U}");

        this.subtype.add(SubType.PLANT);
        this.subtype.add(SubType.HORROR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // {T}: Add one mana of any color.
        this.addAbility(new AnyColorManaAbility());

        // {T}: Add two mana of any one color. Spend this mana only to cast spells from your graveyard.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(2)
                .condition(new FilteredSpellManaCondition(manaFilter))
                .ruleText("Add two mana of any one color. Spend this mana only to cast spells from your graveyard")
                .build()
        );

        // {G}{U}, {T}, Exile Rootcoil Creeper: Return target card with flashback you own in exile to your hand.
        Ability ability = new SimpleActivatedAbility(
                new ReturnToHandTargetEffect()
                        .setText("return target card with flashback you own from exile to your hand"),
                new ManaCostsImpl<>("{G}{U}")
        );
        ability.addCost(new TapSourceCost());
        ability.addCost(new ExileSourceCost());
        ability.addTarget(new TargetCardInExile(filter));
        this.addAbility(ability);
    }

    private RootcoilCreeper(final RootcoilCreeper card) {
        super(card);
    }

    @Override
    public RootcoilCreeper copy() {
        return new RootcoilCreeper(this);
    }
}
