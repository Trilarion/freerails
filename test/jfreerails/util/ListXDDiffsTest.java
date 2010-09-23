/*
 * Created on 27-Jul-2005
 *
 */
package jfreerails.util;

import java.util.SortedMap;
import java.util.TreeMap;

public class ListXDDiffsTest extends ListXDTest {
	
	private SortedMap<ListKey, Object> map;

	enum listid{list1, list2, list3}
    @Override
    protected void setUp() throws Exception {
        super.setUp();


        map  = new TreeMap<ListKey, Object>();
        list1d = new List1DDiff<Object>(map, list1d, listid.list1);
        list2d = new List2DDiff<Object>(map, list2d, listid.list2);
        list3d = new List3DDiff<Object>(map, list3d, listid.list3);
    }

}
