package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.CastFromEverywhereSourceCondition;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.FilterPermanent;
import mage.filter.FilterTyped;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.typed.ability.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.card.CardPredicate;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class TerritoryForge extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent("artifact or land");
    private static final FilterTyped abilityFilter = new FilterTyped("activated ability of a card")
            .add(CardPredicate.instance)
            .add(ActivatedAbilityPredicate.instance);

    static {
        filter.add(Predicates.or(CardType.ARTIFACT.getPredicate(), CardType.LAND.getPredicate()));
    }

    public TerritoryForge(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}{R}");

        // When Territory Forge enters the battlefield, if you cast it, exile target artifact or land.
        Ability ability = new EntersBattlefieldTriggeredAbility(new ExileTargetEffect().setToSourceExileZone(true))
                .withInterveningIf(CastFromEverywhereSourceCondition.instance);
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);

        // Territory Forge has all activated abilities of the exiled card.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .setAbilityFilter(abilityFilter)
                .fromSourceExiled()
                .setText("{this} has all activated abilities of the exiled card")
        ));
    }

    private TerritoryForge(final TerritoryForge card) {
        super(card);
    }

    @Override
    public TerritoryForge copy() {
        return new TerritoryForge(this);
    }
}
