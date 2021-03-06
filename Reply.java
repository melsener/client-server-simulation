//***************************************************************************************************************************************************

import java.util.List ;

//***************************************************************************************************************************************************




//***************************************************************************************************************************************************

public class Reply
{
  //=================================================================================================================================================

  private final String         description ;  // "Index", "Document" or "404"
  private final List< String > content     ;

  //=================================================================================================================================================

  public Reply ( String description , List< String > content )
  {
    this.description = description ;
    this.content     = content     ;
  }

  //=================================================================================================================================================

  @Override public String toString ()
  {
    // TODO
    if(content==null)
        return "[" + description + ":"+ String.join(", ", "NA") + "]";
    return "[" + description + ":"+ String.join(", ", content) + "]";

  }

  //=================================================================================================================================================
}

//***************************************************************************************************************************************************

