package mage.cards.v;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceCounterEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class VorinclexMonstrousRaider extends CardImpl {

    public VorinclexMonstrousRaider(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{G}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.PHYREXIAN);
        this.subtype.add(SubType.PRAETOR);
        this.power = new MageInt(6);
        this.toughness = new MageInt(6);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Haste
        this.addAbility(HasteAbility.getInstance());

        // If you would put one or more counters on a permanent or player, put twice that many of each of those kinds of counters on that permanent or player instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.MULTIPLY, 2)
                .setTargetPermanents(true)
                .setTargetPlayers(true)
                .setEventController(TargetController.YOU)
                .setText("if you would put one or more counters on a permanent or player, " +
                        "put twice that many of each of those kinds of counters on that permanent or player instead")
        ));

        // If an opponent would put one or more counters on a permanent or player, they put half that many of each of those kinds of counters on that permanent or player instead, rounded down.
        this.addAbility(new SimpleStaticAbility(new ReplaceCounterEffect(ReplaceCounterEffect.ModificationType.DIVIDE, 2)
                .setTargetPermanents(true)
                .setTargetPlayers(true)
                .setEventController(TargetController.OPPONENT)
                .setText("If an opponent would put one or more counters on a permanent or player, " +
                        "they put half that many of each of those kinds of counters on that permanent or player instead, rounded down")
        ));
    }

    private VorinclexMonstrousRaider(final VorinclexMonstrousRaider card) {
        super(card);
    }

    @Override
    public VorinclexMonstrousRaider copy() {
        return new VorinclexMonstrousRaider(this);
    }
}
