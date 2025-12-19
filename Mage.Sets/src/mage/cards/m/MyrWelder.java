package mage.cards.m;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterArtifactCard;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCardInGraveyard;

import java.util.UUID;

/**
 * @author BetaSteward_at_googlemail.com
 */
public final class MyrWelder extends CardImpl {

    private static final FilterArtifactCard filter = new FilterArtifactCard("artifact card from a graveyard");

    public MyrWelder(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{3}");
        this.subtype.add(SubType.MYR);
        this.power = new MageInt(1);
        this.toughness = new MageInt(4);

        // Imprint - {tap}: Exile target artifact card from a graveyard
        SimpleActivatedAbility ability = new SimpleActivatedAbility(new MyrWelderEffect(), new TapSourceCost());
        ability.addTarget(new TargetCardInGraveyard(filter));
        this.addAbility(ability.setAbilityWord(AbilityWord.IMPRINT));

        // Myr Welder has all activated abilities of all cards exiled with it
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect(
                StaticFilters.FILTER_ACTIVATED_ABILITY,
                "{this} has all activated abilities of all cards exiled with it")
                .fromSourceImprinted()
        ));

    }

    private MyrWelder(final MyrWelder card) {
        super(card);
    }

    @Override
    public MyrWelder copy() {
        return new MyrWelder(this);
    }

}

class MyrWelderEffect extends OneShotEffect {

    MyrWelderEffect() {
        super(Outcome.Exile);
        staticText = "Exile target artifact card from a graveyard";
    }

    private MyrWelderEffect(final MyrWelderEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = game.getCard(source.getFirstTarget());
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (card != null && permanent != null) {
            card.moveToExile(getId(), "Myr Welder (Imprint)", source, game);
            permanent.imprint(card.getId(), game);
            return true;
        }
        return false;
    }

    @Override
    public MyrWelderEffect copy() {
        return new MyrWelderEffect(this);
    }

}
