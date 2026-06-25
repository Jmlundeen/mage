package mage.cards.b;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.keyword.MorphAbility;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.FilterTyped;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.permanent.TappedPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.game.Game;
import mage.players.Player;
import mage.util.ObjectQuery;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class BirchloreRangers extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent(SubType.ELF, "untapped Elves you control");

    static {
        filter.add(TappedPredicate.UNTAPPED);
    }

    public BirchloreRangers(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}");
        this.subtype.add(SubType.ELF, SubType.DRUID, SubType.RANGER);

        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Tap two untapped Elves you control: Add one mana of any color.
        this.addAbility(new AnyColorManaAbility(Zone.BATTLEFIELD, new TapSourceCost(), BirchloreRangersMaxValue.instance, 1));

        // Morph {G}
        this.addAbility(new MorphAbility(this, new ManaCostsImpl<>("{G}")));
    }

    private BirchloreRangers(final BirchloreRangers card) {
        super(card);
    }

    @Override
    public BirchloreRangers copy() {
        return new BirchloreRangers(this);
    }
}

enum BirchloreRangersMaxValue implements DynamicValue {
    instance;

    private final FilterTyped filter = new FilterTyped("untapped Elf you control")
            .addAll(IMageObjectPredicate.getOSPPredicate(SubType.ELF.getPredicate()),
                    mage.filter.predicate.typed.permanent.status.TappedPredicate.UNTAPPED,
                    TargetController.YOU.getControllerPredicate()
            );

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        if (game == null || sourceAbility == null) {
            return 0;
        }
        Player controller = game.getPlayer(sourceAbility.getControllerId());
        if (controller == null) {
            return 0;
        }
        return ObjectQuery.queryPermanents(game, controller, sourceAbility, filter).size() / 2;
    }

    @Override
    public BirchloreRangersMaxValue copy() {
        return this;
    }

    @Override
    public String getMessage() {
        return "Untapped Elves you control";
    }
}
