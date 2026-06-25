package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldThisOrAnotherTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.ReturnToHandTargetEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.FilterTyped;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.ObjectSourcePlayerPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.util.CardUtil;

import java.util.Set;
import java.util.UUID;

/**
 * @author earchip94
 */
public final class ClementTheWorrywort extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledCreaturePermanent("creature you control with lesser mana value");
    private static final FilterTyped frogFilter = new FilterTyped("Frogs you control")
            .addAll(
                IMageObjectPredicate.getOSPPredicate(SubType.FROG.getPredicate()),
                TargetController.YOU.getControllerPredicate()
            );

    static {
        filter.add(ClementTheWorrywortPredicate.instance);
    }

    public ClementTheWorrywort(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.FROG);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // Whenever Clement, the Worrywort or another creature you control enters, return up to one target creature you control with lesser mana value to its owner's hand.
        Ability ability = new EntersBattlefieldThisOrAnotherTriggeredAbility(
                new ReturnToHandTargetEffect(), StaticFilters.FILTER_CONTROLLED_CREATURE, false, false
        );
        ability.addTarget(new TargetPermanent(0, 1, filter));
        this.addAbility(ability);

        // Frogs you control have "{T}: Add {G} or {U}. Spend this mana only to cast a creature spell."
        Ability manaAbility = ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addChoice(Set.of(ManaType.GREEN, ManaType.BLUE), 1)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_CREATURE_SPELL))
                .ruleText("Add {G} or {U}. Spend this mana only to cast a creature spell")
                .build();
        ability = new SimpleStaticAbility(new GenericContinuousEffect(Outcome.AddAbility, frogFilter)
                .withGainedAbilities(manaAbility)
                .setText("Frogs you control have \"{T}: Add {G} or {U}. Spend this mana only to cast a creature spell.\"")
        );
        this.addAbility(ability);
    }

    private ClementTheWorrywort(final ClementTheWorrywort card) {
        super(card);
    }

    @Override
    public ClementTheWorrywort copy() {
        return new ClementTheWorrywort(this);
    }
}

enum ClementTheWorrywortPredicate implements ObjectSourcePlayerPredicate<Permanent> {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Permanent> input, Game game) {
        return CardUtil.getEffectValueFromAbility(
                        input.getSource(), "permanentEnteringBattlefield", Permanent.class
                )
                .filter(permanent -> input.getObject().getManaValue() < permanent.getManaValue())
                .isPresent();
    }
}
