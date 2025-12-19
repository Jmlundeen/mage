package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCreaturePermanent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author noxx
 */
public final class DarkImpostor extends CardImpl {

    public DarkImpostor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");
        this.subtype.add(SubType.VAMPIRE);
        this.subtype.add(SubType.ASSASSIN);

        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // {4}{B}{B}: Exile target creature and put a +1/+1 counter on Dark Impostor.
        Ability ability = new SimpleActivatedAbility(
                new DarkImpostorExileTargetEffect(), new ManaCostsImpl<>("{4}{B}{B}")
        );
        ability.addEffect(new AddCountersSourceEffect(CounterType.P1P1.createInstance())
                .setText("and put a +1/+1 counter on {this}"));
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);

        // Dark Impostor has all activated abilities of all creature cards exiled with it.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect(StaticFilters.FILTER_ACTIVATED_ABILITY,
                "{this} has all activated abilities of all creature cards exiled with it")
                .fromSourceExiled()
        ));
    }

    private DarkImpostor(final DarkImpostor card) {
        super(card);
    }

    @Override
    public DarkImpostor copy() {
        return new DarkImpostor(this);
    }
}

class DarkImpostorExileTargetEffect extends OneShotEffect {

    DarkImpostorExileTargetEffect() {
        super(Outcome.Exile);
        staticText = "exile target creature";
    }

    private DarkImpostorExileTargetEffect(final DarkImpostorExileTargetEffect effect) {
        super(effect);
    }

    @Override
    public DarkImpostorExileTargetEffect copy() {
        return new DarkImpostorExileTargetEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanent(source.getFirstTarget());
        if (player == null || permanent == null) {
            return false;
        }
        MoveCardsParameters parameters = new MoveCardsParameters(permanent, Zone.EXILED)
                .setExileId(CardUtil.getExileZoneId(game, source))
                .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, null));
        return player.moveCards(parameters, source, game);
    }
}
