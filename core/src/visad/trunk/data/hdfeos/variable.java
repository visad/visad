package visad.data.hdfeos;


  public class variable  {

    String  name;
    int rank;
    int type;
    dimensionSet  dimSet;


    variable( String name, dimensionSet set, int rank, int type ) 
    {
  
       if ( set.getSize() != rank ) 
       {
         /* throw Exception:  problem with dimensionSet size */
       }
 

       this.name = name;
       this.dimSet = set;
       this.type = type;
       this.rank = rank;
    }

    public String getName() 
    {

      String name = this.name;
      return name;
    }

    public int getRank() 
    {
       return rank;
    }

    public boolean equals( variable obj ) 
    {

      if( this.name.equals( obj.getName()) ) {

         return true;
       }
       else {

         return false;
       }
    }

    public dimensionSet getDimSet()
    {
       return dimSet;
    }

    public namedDimension getDim( int ii )
    {
       return dimSet.getElement( ii );

    }

    public int getNumberType()
    {
       return this.type;
    }

    public String toString()  {

       String str = "variable:  "+name+"\n"+
                    "    rank:  "+rank+"\n"+
                    "    type:  "+type+"\n"+"  "+dimSet.toString()+"\n";
       return str;
    }

  }
