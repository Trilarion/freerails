/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Provides classes that encapsulate changes to the game world. The classes implement
 * the GoF request pattern, and they are referred to as 'moves'. All moves should
 * implement the interface {@link freerails.move.Move}. The javadoc comment for
 * the interface move sets out the contract they should obey in addition to implementing
 * the methods it defines. </p>
 * <strong>Guide lines for writing moves</strong></p>
 * (1) Implement the interface Move and follow the contract described in the javadoc
 * for the interface.</p>
 * (2) Rather than writing the class from scratch, it is probably better to extend
 * one of the generic moves class.</p>
 * <table width="75%" border="0">
 * <caption>??</caption>
 * <tr>
 * <td><strong>if the move..</strong></td>
 * <td><strong>then extend</strong></td>
 * </tr>
 * <tr>
 * <td>adds an item to a list</td>
 * <td>{@link freerails.move.listmove.AddItemToListMove}</td>
 * </tr>
 * <tr>
 * <td>removes an item from a list</td>
 * <td>{@link freerails.move.listmove.RemoveItemFromListMove}</td>
 * </tr>
 * <tr>
 * <td>changes an item in a list</td>
 * <td>{@link freerails.move.listmove.ChangeItemInListMove}</td>
 * </tr>
 * <tr>
 * <td>does several things</td>
 * <td>{@link freerails.move.CompositeMove}</td>
 * </tr>
 * </table>
 * (3) Consider writing a junit testcase for the move, and if you do, make the
 * testcase a extend freerails.move.AbstractMoveTestCase.</p>
 * (4) If the move depends on properties stored on the world object, consider
 * extending {@link freerails.move.generator.MoveGenerator}.</p>
 * &nbsp;</p>
 */
package freerails.move;