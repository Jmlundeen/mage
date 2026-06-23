package mage.cards.b;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.NextSpellCastHasAbilityEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.CascadeAbility;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.providers.common.player.TargetPointerManaPlayerProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.predicate.Predicates;
import mage.target.TargetPermanent;
import mage.target.TargetPlayer;

import java.util.UUID;

/**
 *
 * @author notgreat
 */
public final class BiggerOnTheInside extends CardImpl {

    private static final FilterPermanent filter
            = new FilterPermanent("artifact or land");

    static {
        filter.add(Predicates.or(
                CardType.ARTIFACT.getPredicate(),
                CardType.LAND.getPredicate()
        ));
    }

    public BiggerOnTheInside(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{R}{G}");
        this.subtype.add(SubType.AURA);

        // Enchant artifact or land
        TargetPermanent auraTarget = new TargetPermanent(filter);
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.AddAbility));
        this.addAbility(new EnchantAbility(auraTarget));

        // Enchanted permanent has "{T}: Target player adds two mana of any one color. The next spell they cast this turn has cascade."
        Effect manaEffect = ComposedManaAbilityBuilder.builder()
                .addAnyColor(2)
                .playerProvider(TargetPointerManaPlayerProvider.instance)
                .ruleText("Target player adds two mana of any one color")
                .buildEffect();
        Ability gainedAbility = new SimpleActivatedAbility(manaEffect, new TapSourceCost());
        gainedAbility.addEffect(new NextSpellCastHasAbilityEffect(new CascadeAbility(), StaticFilters.FILTER_CARD, TargetController.SOURCE_TARGETS)
                .setText("The next spell they cast this turn has cascade")
        );
        gainedAbility.addTarget(new TargetPlayer());
        Effect effect = new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.ATTACHED_TO)
                .withGainedAbilities(gainedAbility)
                .setText("Enchanted permanent has {gainedAbilitiesQuotes}");
        this.addAbility(new SimpleStaticAbility(effect));
    }

    private BiggerOnTheInside(final BiggerOnTheInside card) {
        super(card);
    }

    @Override
    public BiggerOnTheInside copy() {
        return new BiggerOnTheInside(this);
    }
}
