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
 * <p>The <code>freerails&#46world&#46*</code> packages provide the classes that make up
 * the game-world, the classes in these packages will be used by the client and
 * the server. If at some stage there is a C/C++ version of freerails that works
 * with the java version, then there will need to be C/C++ and java versions of
 * these classes. </p>
 * <p><strong>Properties of the classes</strong></p>
 * <p>(1) The classes need to be serializable so that the game state can be sent
 * over a network from the server to the client when the game starts and so that
 * game states can be loaded and saved to disk. To achieve this, they should implement
 * <code>freerails.world.FreerailsSerializable.</code></p>
 * <p>(2) The classes should override the equals method to test for logical equality.
 * (Two objects are logical equal if their fields have the same values.) Logical
 * equality should survive serialization: i.e. if object A is serialized and then
 * deserialized as B, then A.equals(B) should return true. The rationale for this
 * is it makes writing tests that compare actual and expected states of objects
 * easy.</p>
 * <p>(3) The dependencies between classes that make up the game world (those in <code>freerails.world.*</code>
 * packages ) should only flow in one direction. Eg, the World object has references,
 * either directly or indirectly, to all the objects that make up the game world,
 * so non of the other objects that make up the game world should have references
 * to the world object. Problems where it seems like, say, a station needs a reference
 * to the world object can be solved by adding a third object that is not itself
 * part of the game world but has a reference to the world and loops over the stations
 * or whatever and performs the necessary task.</p>
 * <p>(4) There should only be one chain of references leading from the world object
 * to each object in the game world. E.g. since the world object has a direct reference
 * to each of the stations, other objects in the game world should refer to stations
 * indirectly by storing a station number rather than a reference to a station
 * object.</p>
 * <p>(5) Changes to game world objects should be done using Move objects. There are
 * more details on the <a href="../move/package-summary.html">move package overview</a>.</p>
 * <p>(6) Where possible, game world elements should be immutable or composed of immmutable
 * parts. See Bloch, <em>Effective Java</em>, item 13 or <a
 * href="http://courses.dce.harvard.edu/%7Ecscie160/EffectiveJava.htm" target="_blank">this
 * page</a>.<br>
 */
package freerails.world;