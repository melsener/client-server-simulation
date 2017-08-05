//***************************************************************************************************************************************************

// TODO: Imports

//***************************************************************************************************************************************************

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



//***************************************************************************************************************************************************



public class Client extends Thread
{
  //=================================================================================================================================================

  private final Server   server              ;
  private final int      periodOfRequests    ;
  private final Random random              ;

  //=================================================================================================================================================

  // TODO: Optional extra fields

    private int requestCount;
    private int replyCount;
    private List<String> serverKeys;

  //=================================================================================================================================================

  public Client ( String name , double frequencyOfRequests , Server server )
  {
    super( name ) ;

    this.server           = server                                 ;
    this.periodOfRequests = (int) ( 1000.0 / frequencyOfRequests ) ;
    this.random           = new Random()                           ;

    this.serverKeys = new ArrayList<>();

    start() ;
  }

  //=================================================================================================================================================

  public void acceptReply ( Reply reply ) throws Exception
  {
    // TODO
      Runner.logf("%-9s: Got reply. %s%n",this.getName() , reply.toString());
      if(replyCount==0)
      {
          //save first request to use
          Class cl = reply.getClass();
          Field content = cl.getDeclaredField("content");
          content.setAccessible(true);
          Object o = content.get(reply);
          ArrayList<String> aL =((ArrayList<String>) o);
          for(String s : aL)
              serverKeys.add(s);
      }
      replyCount++;
  }

  //=================================================================================================================================================

  @Override public void run ()
  {
    // TODO

      /*Request newReq = generateRequest();
      while(server.acceptRequest(newReq))
      {   requestCount++;
          try {
              sleep(periodOfRequests);
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
          newReq = generateRequest();
      }*/
      Request newReq;
      boolean isRunning = true;
      while(isRunning){
          try {
              sleep(periodOfRequests);
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
          newReq = generateRequest();
          isRunning = server.acceptRequest(newReq);
          if(!isRunning)
              break;
          //Runner.logf("%-9s: Sent request " + newReq + "%n", this.getName());
          requestCount++;
      }

      synchronized (server) {
          Runner.logf("%-9s: Reporting...%n", getName());
          Runner.logf("            - Number of requests sent    = %d %n", requestCount);
          Runner.logf("            - Number of replies received = %d %n", replyCount);
          server.notify();
      }
  }

  //=================================================================================================================================================

  private Request generateRequest ()
  {
    // TODO
      if(serverKeys.size()==0)
      {
          return new Request(this,"Get","Index",null);
      }
      else
      {   int method = random.nextInt(2);
          int index= random.nextInt(serverKeys.size());
          int garbage=random.nextInt(100);
          Request newRequest;
          if(method==0) {
              if(garbage<95)
                  newRequest = new Request(this, "Get", serverKeys.get(index), null);
              else
                  newRequest = new Request(this, "Get", "Z", null);
          }
          else{
              int dataID = random.nextInt(1000-100)+100;
              if(garbage<95)
                newRequest = new Request(this,"Post",serverKeys.get(index),"Data" +dataID);
              else
                  newRequest = new Request(this,"Post","Z","Data" +dataID);

          }
          //requestCount++;
          return newRequest;
      }
  }

  //=================================================================================================================================================

  // TODO: Optional extra helper methods

  //=================================================================================================================================================
}

//***************************************************************************************************************************************************

