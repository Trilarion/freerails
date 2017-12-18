/**
 * <p>Provides classes that encapsulate changes to the game world. The classes implement
 * the GoF request pattern, and they are referred to as 'moves'. All moves should
 * implement the interface {@link freerails.move.Move}. The javadoc comment for
 * the interface move sets out the contract they should obey in addition to implementing
 * the methods it defines. </p>
 * <p><strong>Guide lines for writing moves</strong></p>
 * <p>(1) Implement the interface Move and follow the contract described in the javadoc
 * for the interface.</p>
 * <p>(2) Rather than writing the class from scratch, it is probably better to extend
 * one of the generic moves class.</p>
 * <table width="75%" border="0">
 * <tr>
 * <td><strong>if the move..</strong></td>
 * <td><strong>then extend</strong></td>
 * </tr>
 * <tr>
 * <td>adds an item to a list</td>
 * <td>{@link freerails.move.AddItemToListMove}</td>
 * </tr>
 * <tr>
 * <td>removes an item from a list</td>
 * <td>{@link freerails.move.RemoveItemFromListMove}</td>
 * </tr>
 * <tr>
 * <td>changes an item in a list</td>
 * <td>{@link freerails.move.ChangeItemInListMove}</td>
 * </tr>
 * <tr>
 * <td>does several things</td>
 * <td>{@link freerails.move.CompositeMove}</td>
 * </tr>
 * </table>
 * <p>(3) Consider writing a junit testcase for the move, and if you do, make the
 * testcase a extend {@link freerails.move.AbstractMoveTestCase}.</p>
 * <p>(4) If the move depends on properties stored on the world object, consider
 * extending {@link freerails.controller.PreMove}.</p>
 * <p>&nbsp;</p>
 */
package freerails.move;