
package jfreerails.world.train;



public class WagonType {
    
    public static final int NUMBER_OF_CATEGORIES=6;
    
    public static final int MAIL=0;
    
    
    public static final int PASSENGER=1;
    
    
    public static final int FAST_FREIGHT=2;
    
    
    public static final int SLOW_FREIGHT=3;
    
    
    public static final int BULK_FREIGHT=4;
    
    
    public static final int ENGINE=5;
    
    private final String typeName;
    
    private final int typeCategory;
    
    public WagonType(String name, int category){
        typeName=name;
        typeCategory=category;
    }
    
    public String getName(){
        return typeName;
    }
    
    public int getCategory(){
        return typeCategory;
    }
    
    
}
