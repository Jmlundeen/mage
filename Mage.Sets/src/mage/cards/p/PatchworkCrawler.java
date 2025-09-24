package mage.cards.p;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ExileTargetForSourceEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCardInYourGraveyard;
import mage.util.CardUtil;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class PatchworkCrawler extends CardImpl {

    public PatchworkCrawler(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.subtype.add(SubType.ZOMBIE);
        this.subtype.add(SubType.HORROR);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // {2}{U}: Exile target creature card from your graveyard and put a +1/+1 counter on Patchwork Crawler.
        Ability ability = new SimpleActivatedAbility(new ExileTargetForSourceEffect(), new ManaCostsImpl<>("{2}{U}"));
        ability.addEffect(new AddCountersSourceEffect(CounterType.P1P1.createInstance()).concatBy("and"));
        ability.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_CREATURE_YOUR_GRAVEYARD));
        this.addAbility(ability);

        // Patchwork Crawler has all activated abilities of all creature cards exiled with it.
        this.addAbility(new SimpleStaticAbility(new PatchworkCrawlerEffect()));
    }

    private PatchworkCrawler(final PatchworkCrawler card) {
        super(card);
    }

    @Override
    public PatchworkCrawler copy() {
        return new PatchworkCrawler(this);
    }
}

class PatchworkCrawlerEffect extends ContinuousEffectImpl {

    PatchworkCrawlerEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "{this} has all activated abilities of all creature cards exiled with it";
    }

    private PatchworkCrawlerEffect(final PatchworkCrawlerEffect effect) {
        super(effect);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        ExileZone exileZone = game.getExile().getExileZone(CardUtil.getExileZoneId(game, source));
        Set<Ability> abilities = exileZone.getCards(StaticFilters.FILTER_CARD_CREATURE, game)
                .stream()
                .flatMap(card -> card.getAbilities(game).stream())
                .filter(Ability::isActivatedAbility)
                .collect(java.util.stream.Collectors.toSet());
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            for (Ability ability : abilities) {
                permanent.addAbility(ability, source.getSourceId(), game, true);
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        ExileZone exileZone = game.getExile().getExileZone(CardUtil.getExileZoneId(game, source));
        if (permanent == null || exileZone == null || exileZone.isEmpty()) {
            return false;
        }
        affectedObjects.add(permanent);
        return true;
    }

    @Override
    public PatchworkCrawlerEffect copy() {
        return new PatchworkCrawlerEffect(this);
    }
}
