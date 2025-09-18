package mage.cards.j;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.keyword.DeathtouchAbility;
import mage.abilities.keyword.LifelinkAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.token.ImpToken;
import mage.game.stack.Spell;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author DominionSpy
 */
public final class JudithCarnageConnoisseur extends CardImpl {

    public JudithCarnageConnoisseur(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SHAMAN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Whenever you cast an instant or sorcery spell, choose one --
        // * That spell gains deathtouch and lifelink.
        Ability ability = new SpellCastControllerTriggeredAbility(
                new JudithCarnageConnoisseurEffect(),
                StaticFilters.FILTER_SPELL_AN_INSTANT_OR_SORCERY,
                false, SetTargetPointer.SPELL);

        // * Create a 2/2 red Imp creature token with "When this creature dies, it deals 2 damage to each opponent."
        Mode mode = new Mode(new CreateTokenEffect(new ImpToken()));
        ability.addMode(mode);
        this.addAbility(ability);
    }

    private JudithCarnageConnoisseur(final JudithCarnageConnoisseur card) {
        super(card);
    }

    @Override
    public JudithCarnageConnoisseur copy() {
        return new JudithCarnageConnoisseur(this);
    }
}

class JudithCarnageConnoisseurEffect extends ContinuousEffectImpl {

    JudithCarnageConnoisseurEffect() {
        super(Duration.Custom, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "That spell gains deathtouch and lifelink";
    }

    private JudithCarnageConnoisseurEffect(final JudithCarnageConnoisseurEffect effect) {
        super(effect);
    }

    @Override
    public JudithCarnageConnoisseurEffect copy() {
        return new JudithCarnageConnoisseurEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Card card = (Card) object;
            game.getState().addOtherAbility(card, DeathtouchAbility.getInstance());
            game.getState().addOtherAbility(card, LifelinkAbility.getInstance());
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Spell spell = game.getSpell(getTargetPointer().getFirst(game, source));
        if (spell == null) {
            discard();
            return false;
        }

        affectedObjects.add(spell.getCard());
        return true;
    }
}
