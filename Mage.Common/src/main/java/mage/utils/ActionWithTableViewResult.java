package mage.utils;

import mage.ws.v1.view.ViewProto;

/**
 * Used to write less code for ActionWithResult anonymous classes with TableView return type.
 *
 * @author noxx
 */
public abstract class ActionWithTableViewResult extends ActionWithNullNegativeResult<ViewProto.TableView> {
}
