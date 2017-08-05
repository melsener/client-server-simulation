//***************************************************************************************************************************************************

// TODO: Imports

//***************************************************************************************************************************************************

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


//***************************************************************************************************************************************************



public class Server extends Thread
{
  //=================================================================================================================================================

  private boolean                                isRunning    ;
  private final Map< String , List< String >> content      ;
  private final Queue< Request                 > requestQueue ;

  //=================================================================================================================================================

  // TODO: Optional extra fields
    private int requestCount;
    private int replyCount;

  //=================================================================================================================================================

  public Server ( String name )
  {
    super( name ) ;

    isRunning    = true               ;
    content      = new HashMap<>() ;  generateContent() ;
    requestQueue = new LinkedList<>() ;

    Timer     timer     = new Timer    () ;
    TimerTask timerTask = new TimerTask()
                          {
                            @Override public void run()
                            {
                              Runner.logf( "Timer    : Server shutting down...%n" ) ;

                              isRunning = false ;

                              // TODO: Optional extra statements here
                                synchronized (requestQueue)
                                {   requestQueue.notify();
                                }

                              timer.cancel() ;
                            }
                          } ;

    timer.schedule( timerTask , 10 * 1000 ) ;


    start() ;
  }

  //=================================================================================================================================================

  public boolean acceptRequest ( Request request )
  {
    // TODO
      if (isRunning) {
          synchronized (requestQueue) {
              requestQueue.add(request);
              requestCount++;
              Runner.logf("%-9s: Sent request " + request + "%n", Thread.currentThread().getName());
              requestQueue.notifyAll();
          }
      }
    return isRunning ;
  }

  //=================================================================================================================================================

  @Override public void run ()
  {
    // TODO
    boolean checker = false;
    while(isRunning)
    {
        synchronized (requestQueue)
        {
            while(requestQueue.isEmpty())
            {   if(!isRunning)
                    break;
                try {
                    Runner.logf("%-9s: Request queue is empty, waiting...%n",this.getName());
                    requestQueue.wait();
                    if(!isRunning)
                        break;
                    Runner.logf("%-9s: Has just been notified, getting back to work...%n",this.getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(!isRunning)
                break;
            Request r = requestQueue.poll();
            //Validation of Request
            checker= checkValid(r);
            if(checker)
            {
                Runner.logf("%-9s: Request %s is VALID, handling...%n",this.getName(),r.toString());

                //If GET request, reply request.
                if(requiresResponse(r))
                    invokeReply(r);
                else
                    handlePostRequest(r);

            }
            else
            {
                Runner.logf("%-9s: Request %s is INVALID, dropped...%n",this.getName(),r.toString());
            }
        }
    }
    report();



  }

  //=================================================================================================================================================

  private void generateContent ()
  {
    String         key   ;
    List< String > value ;

    key = "A" ;  value = new ArrayList<>() ;  value.add( "A1" ) ;  value.add( "A2" ) ;  content.put( key , value ) ;
    key = "B" ;  value = new ArrayList<>() ;  value.add( "B1" ) ;  value.add( "B2" ) ;  content.put( key , value ) ;
    key = "C" ;  value = new ArrayList<>() ;  value.add( "C1" ) ;  value.add( "C2" ) ;  content.put( key , value ) ;
    key = "D" ;  value = new ArrayList<>() ;  value.add( "D1" ) ;  value.add( "D2" ) ;  content.put( key , value ) ;
  }

  //=================================================================================================================================================

  // TODO: Optional extra helper methods

    private boolean isStartRequest(Request request)
    {
        Class cl = request.getClass();
        boolean flag=true;
        try
        {
            Field method = cl.getDeclaredField("method");
            Field uri = cl.getDeclaredField("uri");
            method.setAccessible(true);
            uri.setAccessible(true);
            flag= method.get(request).equals("Get") && uri.get(request).equals("Index");
            method.setAccessible(false);
            uri.setAccessible(false);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return flag;

    }

    private boolean checkValid(Request request)
    {
            Class<?> cl = request.getClass();
            boolean isValid=false;
            try {
                Method m = cl.getDeclaredMethod("isValid");
                m.setAccessible(true);

                try {
                    isValid=(boolean) m.invoke(request);
                    m.setAccessible(false);
                }
                catch( IllegalAccessException e )
                {
                    e.printStackTrace();
                }
                catch( InvocationTargetException e )
                {
                    e.printStackTrace();
                    System.err.println( e.getTargetException( ));
                }

            }
            catch(NoSuchMethodException e) {
              e.printStackTrace();
            }
            return isValid;
    }

    private Reply generateReply(Request r)
    {

        if(isStartRequest(r))
        {   List<String> l = new ArrayList<String>(content.keySet());
            return new Reply("Index",l);
        }
        else
        {
            Class cl = r.getClass();
            Reply rep = null;
            try {
                Field method = cl.getDeclaredField("method");
                Field uri = cl.getDeclaredField("uri");
                Field parameter = cl.getDeclaredField("parameter");


                method.setAccessible(true);
                uri.setAccessible(true);
                parameter.setAccessible(true);

                if(method.get(r).equals("Get"))
                {
                    String key = (String) uri.get(r);
                    if(content.containsKey(key))
                    {
                        rep = new Reply("Document",content.get(key));
                    }
                    else
                    {
                        //check this
                        rep =new Reply("404",null);
                    }
                }
                method.setAccessible(false);
                uri.setAccessible(false);
                parameter.setAccessible(false);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return rep;
        }

    }

    private void invokeReply(Request r)
    {
        Reply rep = generateReply(r);
        Class cl = r.getClass();

        try {
            //Get request's client
            Field client = cl.getDeclaredField("client");
            client.setAccessible(true);
            Object o = client.get(r);
            Method acceptReply= client.getType().getDeclaredMethod("acceptReply",rep.getClass());

            replyCount++;

            acceptReply.invoke(o,rep);
            client.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        catch (InvocationTargetException e){
            //InvocationTargetException - if the underlying method throws an exception.
            e.getCause().printStackTrace();
        }

    }

    private boolean requiresResponse(Request r)
    {   //If GET request return true;
        Class cl = r.getClass();
        boolean response=false;

        try {
            Field method = cl.getDeclaredField("method");
            method.setAccessible(true);
            if(method.get(r).equals("Get"))
                response=true;
            method.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return response;
    }

    private void handlePostRequest(Request r)
    {
            Class cl = r.getClass();
            try {
                Field method = cl.getDeclaredField("method");
                Field uri = cl.getDeclaredField("uri");
                Field parameter = cl.getDeclaredField("parameter");


                method.setAccessible(true);
                uri.setAccessible(true);
                parameter.setAccessible(true);

                if(method.get(r).equals("Post"))
                {
                    String key = (String) uri.get(r);
                    if(content.containsKey(key))
                    {
                        Runner.logf("%-9s: Post data %s on %s handled successfully.%n",this.getName(),parameter.get(r),key);
                    }
                    else
                    {
                        Runner.logf("%-9s: Post data %s on %s has invalid target uri, ignored!%n",this.getName(),parameter.get(r),key);
                    }
                }
                method.setAccessible(false);
                uri.setAccessible(false);
                parameter.setAccessible(false);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

    }

    private void report()
    {
        Runner.logf("%-9s: Reporting...%n",this.getName());
        Runner.logf("       - Number of requests received = %d %n",requestCount );
        Runner.logf("       - Number of replies sent      = %d %n",replyCount );
    }
  //=================================================================================================================================================
}

//***************************************************************************************************************************************************

