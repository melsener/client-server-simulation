//***************************************************************************************************************************************************

public class Request
{
  //=================================================================================================================================================

  private final Client client    ;  // Client that generates the request
  private final String method    ;  // "Get" or "Post"
  private final String uri       ;  // "Index" or entry chosen from the index (of available documents on the server)
  private final String parameter ;  // Used with "Post" requests only, otherwise null

  //=================================================================================================================================================

  private boolean isValid ()  { return ( Math.random() > 0.2 ) ; }

  //=================================================================================================================================================

  public Request ( Client client , String method , String uri , String parameter )
  {
    this.client    = client    ;
    this.method    = method    ;
    this.uri       = uri       ;
    this.parameter = parameter ;
  }

  //=================================================================================================================================================

  @Override public String toString ()
  {
    // TODO
    if(parameter==null)
        return "[" + client.getName() + ":" + method + ":" + uri + ":" + "NA" + "]";
    return "[" + client.getName() + ":" + method + ":" + uri + ":" + parameter + "]";
  }

  //=================================================================================================================================================
}

//***************************************************************************************************************************************************

