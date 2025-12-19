package mage.cards.k;

import mage.MageInt;
import mage.MageItem;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.LifelinkAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterMana;
import mage.filter.FilterSpell;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author ssouders412
 */
public final class KrrikSonOfYawgmoth extends CardImpl {

    private static final FilterSpell filterSpell = new FilterSpell("a black spell");

    static {
        filterSpell.add(new ColorPredicate(ObjectColor.BLACK));
    }

    public KrrikSonOfYawgmoth(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{B/P}{B/P}{B/P}");
        
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.PHYREXIAN);
        this.subtype.add(SubType.HORROR);
        this.subtype.add(SubType.MINION);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // ({B/P} can be paid with either {B} or 2 life.)
        
        // Lifelink
        this.addAbility(LifelinkAbility.getInstance());

        // For each {B} in a cost, you may pay 2 life rather than pay that mana.
        this.addAbility(new SimpleStaticAbility(new KrrikSonOfYawgmothPhyrexianEffect()));

        // Whenever you cast a black spell, put a +1/+1 counter on K'rrik, Son of Yawgmoth.
        this.addAbility(new SpellCastControllerTriggeredAbility(new AddCountersSourceEffect(CounterType.P1P1.createInstance()), filterSpell, false));
    }

    private KrrikSonOfYawgmoth(final KrrikSonOfYawgmoth card) {
        super(card);
    }

    @Override
    public KrrikSonOfYawgmoth copy() {
        return new KrrikSonOfYawgmoth(this);
    }
}

class KrrikSonOfYawgmothPhyrexianEffect extends ContinuousEffectImpl {

    KrrikSonOfYawgmothPhyrexianEffect() {
        super(Duration.WhileOnBattlefield, Layer.PlayerEffects, SubLayer.NA, Outcome.Benefit);
        this.staticText = "for each {B} in a cost, you may pay 2 life rather than pay that mana";
    }

    private KrrikSonOfYawgmothPhyrexianEffect(final KrrikSonOfYawgmothPhyrexianEffect effect) {
        super(effect);
    }

    @Override
    public KrrikSonOfYawgmothPhyrexianEffect copy() {
        return new KrrikSonOfYawgmothPhyrexianEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        FilterMana filterMana = new FilterMana();
        filterMana.setBlack(true);
        for (MageItem object : affectedObjects) {
            ((Player) object).addPhyrexianToColors(filterMana);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent sourcePermanent = game.getPermanent(source.getSourceId());
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null && sourcePermanent != null) {
            affectedObjects.add(controller);
            return true;
        }
        return false;
    }
}
