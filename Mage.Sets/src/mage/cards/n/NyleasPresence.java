package mage.cards.n;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.mana.*;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.target.common.TargetLandPermanent;

import java.util.List;
import java.util.UUID;

/**
 * @author LevelX2
 */
public final class NyleasPresence extends CardImpl {

    public NyleasPresence(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{G}");
        this.subtype.add(SubType.AURA);

        // Enchant land
        TargetPermanent auraTarget = new TargetLandPermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.AddAbility));
        Ability ability = new EnchantAbility(auraTarget);
        this.addAbility(ability);

        // When Nylea's Presence enters the battlefield, draw a card.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new DrawCardSourceControllerEffect(1)));

        // Enchanted land is every basic land type in addition to its other types.
        this.addAbility(new SimpleStaticAbility(new NyleasPresenceLandTypeEffect()));
    }

    private NyleasPresence(final NyleasPresence card) {
        super(card);
    }

    @Override
    public NyleasPresence copy() {
        return new NyleasPresence(this);
    }
}

class NyleasPresenceLandTypeEffect extends ContinuousEffectImpl {

    private static final Ability[] basicManaAbilities = {
            new WhiteManaAbility(),
            new BlueManaAbility(),
            new BlackManaAbility(),
            new RedManaAbility(),
            new GreenManaAbility()
    };

    NyleasPresenceLandTypeEffect() {
        super(Duration.WhileOnBattlefield, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Detriment);
        this.staticText = "Enchanted land is every basic land type in addition to its other types";
    }

    private NyleasPresenceLandTypeEffect(final NyleasPresenceLandTypeEffect effect) {
        super(effect);
    }

    @Override
    public NyleasPresenceLandTypeEffect copy() {
        return new NyleasPresenceLandTypeEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            // Optimization: Remove basic mana abilities since they are redundant with AnyColorManaAbility
            //               and keeping them will only produce too many combinations inside ManaOptions
            for (Ability basicManaAbility : basicManaAbilities) {
                if (permanent.getAbilities(game).containsRule(basicManaAbility)) {
                    permanent.removeAbility(basicManaAbility, source.getSourceId(), game);
                }
            }
            // Add the {T}: Add one mana of any color ability
            // This is functionally equivalent to having five "{T}: Add {COLOR}" for each COLOR in {W}{U}{B}{R}{G}
            AnyColorManaAbility ability = new AnyColorManaAbility();
            if (!permanent.getAbilities(game).containsRule(ability)) {
                permanent.addAbility(ability, source.getSourceId(), game);
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent enchantment = game.getPermanent(source.getSourceId());
        if (enchantment == null || enchantment.getAttachedTo() == null) {
            return false;
        }
        Permanent land = game.getPermanent(enchantment.getAttachedTo());
        if (land == null) {
            return false;
        }
        affectedObjects.add(land);
        return true;
    }
}
