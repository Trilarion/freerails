package jfreerails.world.common;

import java.io.Serializable;


/** This interface tags classes that can be sent between the client and the server.
 *
 * <b><p>Every class that implements this interface should be immutable.  There is
 * a an target that uses ConstJava which can be used to check for mutability problems.</b>
 * @author Luke
 * */
public interface FreerailsSerializable extends Serializable {
}