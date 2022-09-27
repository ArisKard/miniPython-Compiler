  import minipython.analysis.*;
import minipython.node.*;
import java.util.*;

public class myvisitor extends DepthFirstAdapter
{
	private Hashtable symtable;
  public HashSet<String> funcset;
  public LinkedList<String> funclist;

	myvisitor(Hashtable symtable)
	{
		this.symtable = symtable;
    funcset = new HashSet<String>();
    funclist = new LinkedList<String>();
	}

//error type 2
  public void outAGoal(AGoal node)
  {
    for (int i=0; i<funclist.size(); i++)
    {
      if (!funcset.contains(funclist.get(i)))
      {
        System.out.printf("- Function name: %s arguments called via another function has not been defined (error type 2)%n", funclist.get(i));
      }
    }
  }

//error type 2,3,7
  public void inAFuncFunction(AFuncFunction node)
  {

    String fName = node.getId().toString();
    int line = node.getId().getLine();
    int pos = node.getId(). getPos();

    LinkedList list = ((LinkedList) node.getArgument());
    int defaults = 0;
    boolean b = true;

    if (!symtable.contains(fName))
    {
      symtable.put(fName, node);
    }
//error type 7
    for(int i=0; i<list.size(); i++)
    {
      LinkedList l = ((LinkedList)((AArgArgument) list.get(i)).getStatement());
      if (l.size() == 1)
      {
        defaults+=1;
      }
    }

    for (int i=list.size()-defaults; i<list.size()+1; i++)
    {
      if (symtable.containsKey(fName + String.valueOf(i)))
      {
        b = false;
        break;
      }
    }

    if (!b)
    {
      System.out.printf("- Line %d: Pos %d: Function with name: %s() and this number of arguments has already been defined (error type 7)%n", line, pos, fName);
    }
    else
    {
      for (int i=list.size()-defaults; i<list.size()+1; i++)
      {
          symtable.put(fName + String.valueOf(i), node);
      }
    }
//error type 7 end

//error type 2
    funcset.add(fName + String.valueOf(list.size()));

    if(node.getStatement() instanceof AReturnStatement)
    {
      if (((AReturnStatement) node.getStatement()).getExpression() instanceof AFunctioncallExpression)
      {
        LinkedList list2 = ((LinkedList)((AFunctioncallExpression)((AReturnStatement) node.getStatement()).getExpression()).getExpression());

        if (list2.size()>0)
        {
          LinkedList l2 = ((LinkedList)((AArglistExpression) list2.get(0)).getExpression());
          funclist.add(((AFunctioncallExpression)((AReturnStatement) node.getStatement()).getExpression()).getId().toString() + String.valueOf(l2.size()));
        }
        else
        {
          funclist.add(((AFunctioncallExpression)((AReturnStatement) node.getStatement()).getExpression()).getId().toString() + String.valueOf(0));
        }

      }
      if (((AReturnStatement) node.getStatement()).getExpression() instanceof AFunctioncalldotExpression)
      {
        LinkedList list2 = ((LinkedList)((AFunctioncalldotExpression)((AReturnStatement) node.getStatement()).getExpression()).getRe());

        if (list2.size()>0)
        {
          LinkedList l2 = ((LinkedList)((AArglistExpression) list2.get(0)).getExpression());
          funclist.add(((AFunctioncalldotExpression)((AReturnStatement) node.getStatement()).getExpression()).getR().toString() + String.valueOf(l2.size()));
        }
        else
        {
          funclist.add(((AFunctioncalldotExpression)((AReturnStatement) node.getStatement()).getExpression()).getR().toString() + String.valueOf(0));
        }
      }
    }

    if(node.getStatement() instanceof AFunctioncallStatement)
    {
      LinkedList list2 = ((LinkedList)((AFunctioncallStatement) node.getStatement()).getExpression());

      if (list2.size()>0)
      {
        LinkedList l2 = ((LinkedList)((AArglistExpression) list2.get(0)).getExpression());
        funclist.add(((AFunctioncallStatement) node.getStatement()).getId().toString() + String.valueOf(0));
      }
      else
      {
        funclist.add(((AFunctioncallStatement) node.getStatement()).getId().toString() + String.valueOf(0));
      }
    }
  }

//error type 3
 public void inAFunctioncallExpression(AFunctioncallExpression node)
 {
   String fName = node.getId().toString();
   int line = node.getId().getLine();
   int pos = node.getId().getPos();

   if(!symtable.containsKey(fName) && !(node.parent().parent() instanceof AFuncFunction))
   {
     System.out.printf("- Line %d: Pos %d: There is no such function (error type 2)%n", line, pos);
   }

   if(symtable.containsKey(fName))
   {
     Object n = symtable.get(fName);
     LinkedList list = ((LinkedList)((AFuncFunction) n).getArgument());
     int nonDefaults = list.size();

     for(int i=0; i<list.size(); i++)
     {
       LinkedList l = ((LinkedList)((AArgArgument) list.get(i)).getStatement());
       if (l.size() == 1)
       {
         nonDefaults-=1;
       }
     }

     LinkedList list2 = ((LinkedList)((AFunctioncallExpression) node).getExpression());
     int m = 0;
     LinkedList l2 = list2;

     if (list2.size()>0)
     {
       l2 = ((LinkedList)((AArglistExpression) list2.get(0)).getExpression());
       m = l2.size();
     }

     if(m < nonDefaults || m>list.size())
     {
        System.out.printf("- Line %s: The function needs %d to %d arguments to be called properly and here there are %d passed (error type 3)%n", line, nonDefaults, list.size(), m);
     }
     else
     {
       AAssignStatement ass = null;

       for (int i=0; i<m; i++)
       {
         if (l2.get(i) instanceof ANumbExpression)
         {
           ANumbExpression num = new ANumbExpression(((ANumbExpression)l2.get(i)).getNumber());
           Object obj = ((AArgArgument)list.get(i)).clone();
           AArgArgument arg = ((AArgArgument)obj);
           TId t = arg.getId();
           ass = new AAssignStatement(t, num);
           String name = (t.toString());
           symtable.put(name, ass);
         }
         else if (l2.get(i) instanceof AStringdoubleqExpression)
         {
           AStringdoubleqExpression strd = new AStringdoubleqExpression(((AStringdoubleqExpression)l2.get(i)).getStringdoubleq());
           Object obj = ((AArgArgument)list.get(i)).clone();
           AArgArgument arg = ((AArgArgument)obj);
           TId t = arg.getId();
           ass = new AAssignStatement(t, strd);
           String name = (t.toString());

           symtable.put(name, ass);

         }
         else if (l2.get(i) instanceof AStringsingleqExpression)
         {
           AStringsingleqExpression strs = new AStringsingleqExpression(((AStringsingleqExpression)l2.get(i)).getStringsingleq());
           Object obj = ((AArgArgument)list.get(i)).clone();
           AArgArgument arg = ((AArgArgument)obj);
           TId t = arg.getId();
           ass = new AAssignStatement(t, strs);
           String name = (t.toString());

           symtable.put(name, ass);
         }
       }

         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof AAdditionExpression)
         {
           AAdditionExpression ade = new AAdditionExpression(((AAdditionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((AAdditionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inAAdditionExpression(ade);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof ASubtractionExpression)
         {
           ASubtractionExpression sube = new ASubtractionExpression(((ASubtractionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((ASubtractionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inASubtractionExpression(sube);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof AMultiplicationExpression)
         {
           AMultiplicationExpression multe = new AMultiplicationExpression(((AMultiplicationExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((AMultiplicationExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
           inAMultiplicationExpression(multe);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof ADivisionExpression)
         {
           ADivisionExpression dive = new ADivisionExpression(((ADivisionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((ADivisionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inADivisionExpression(dive);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof AModExpression)
         {
           AModExpression mode = new AModExpression(((AModExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((AModExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inAModExpression(mode);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof APowerExpression)
         {
           APowerExpression powe = new APowerExpression(((APowerExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((APowerExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inAPowerExpression(powe);
         }

       symtable.put(fName+"call", node);
     }
   }
 }

 //error type 3
 public void inAFunctioncalldotExpression(AFunctioncalldotExpression node)
 {
   String fName = node.getR().toString();
   int line = node.getR().getLine();
   int pos = node.getR().getLine();

   if(!symtable.containsKey(fName) && !(node.parent().parent() instanceof AFuncFunction))
   {
     System.out.printf("- Line %d: Pos %d: There is no such function (error type 2)%n", line, pos);
   }

   if(symtable.containsKey(fName))
   {
     Object n = symtable.get(fName);
     LinkedList list = ((LinkedList)((AFuncFunction) n).getArgument());
     int nonDefaults = list.size();
     for(int i=0; i<list.size(); i++)
     {
       LinkedList l = ((LinkedList)((AArgArgument) list.get(i)).getStatement());
       if (l.size() == 1)
       {
         nonDefaults-=1;
       }
     }

     LinkedList list2 = ((LinkedList)((AFunctioncalldotExpression) node).getRe());
     int m = 0;
     LinkedList l2 = list2;

     if (list2.size()>0)
     {
       l2 = ((LinkedList)((AArglistExpression) list2.get(0)).getExpression());
       m = l2.size();
     }

     if(m < nonDefaults || m>list.size())
     {
       System.out.printf("- Line %s: The function needs %d to %d arguments to be called properly and here there are %d passed (error type 3)%n", line, nonDefaults, list.size(), m);
     }
     else
     {
       AAssignStatement ass = null;

       for (int i=0; i<m; i++)
       {
         if (l2.get(i) instanceof ANumbExpression)
         {
           ANumbExpression num = new ANumbExpression(((ANumbExpression)l2.get(i)).getNumber());
           Object obj = ((AArgArgument)list.get(i)).clone();
           AArgArgument arg = ((AArgArgument)obj);
           TId t = arg.getId();
           ass = new AAssignStatement(t, num);
           String name = (t.toString());
           symtable.put(name, ass);
         }
         else if (l2.get(i) instanceof AStringdoubleqExpression)
         {
           AStringdoubleqExpression strd = new AStringdoubleqExpression(((AStringdoubleqExpression)l2.get(i)).getStringdoubleq());
           Object obj = ((AArgArgument)list.get(i)).clone();
           AArgArgument arg = ((AArgArgument)obj);
           TId t = arg.getId();
           ass = new AAssignStatement(t, strd);
           String name = (t.toString());
           symtable.put(name, ass);

         }
         else if (l2.get(i) instanceof AStringsingleqExpression)
         {
           AStringsingleqExpression strs = new AStringsingleqExpression(((AStringsingleqExpression)l2.get(i)).getStringsingleq());
           Object obj = ((AArgArgument)list.get(i)).clone();
           AArgArgument arg = ((AArgArgument)obj);
           TId t = arg.getId();
           ass = new AAssignStatement(t, strs);
           String name = (t.toString());
           symtable.put(name, ass);
         }
       }

         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof AAdditionExpression)
         {
           AAdditionExpression ade = new AAdditionExpression(((AAdditionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((AAdditionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inAAdditionExpression(ade);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof ASubtractionExpression)
         {
           ASubtractionExpression sube = new ASubtractionExpression(((ASubtractionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((ASubtractionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inASubtractionExpression(sube);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof AMultiplicationExpression)
         {
           AMultiplicationExpression multe = new AMultiplicationExpression(((AMultiplicationExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((AMultiplicationExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
           inAMultiplicationExpression(multe);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof ADivisionExpression)
         {
           ADivisionExpression dive = new ADivisionExpression(((ADivisionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((ADivisionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inADivisionExpression(dive);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof AModExpression)
         {
           AModExpression mode = new AModExpression(((AModExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((AModExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inAModExpression(mode);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof APowerExpression)
         {
           APowerExpression powe = new APowerExpression(((APowerExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((APowerExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inAPowerExpression(powe);
         }

       symtable.put(fName+"call", node);
     }
   }
 }

//error type 3
 public void inAFunctioncallStatement(AFunctioncallStatement node)
 {
   String fName = node.getId().toString();
   int line = node.getId().getLine();
   int pos = node.getId().getLine();

   if(!symtable.containsKey(fName) && !(node.parent().parent() instanceof AFuncFunction))
   {
     System.out.printf("- Line %d: Pos %d: There is no such function (error type 2)%n", line, pos);
   }

   if(symtable.containsKey(fName))
   {
     Object n = symtable.get(fName);
     LinkedList list = ((LinkedList)((AFuncFunction) n).getArgument());
     int nonDefaults = list.size();
     for(int i=0; i<list.size(); i++)
     {
       LinkedList l = ((LinkedList)((AArgArgument) list.get(i)).getStatement());
       if (l.size() == 1)
       {
         nonDefaults-=1;
       }
     }

     LinkedList list2 = ((LinkedList)((AFunctioncallStatement) node).getExpression());
     int m = 0;
     LinkedList l2=list2;

     if (list2.size()>0)
     {
       l2 = ((LinkedList)((AArglistExpression) list2.get(0)).getExpression());
       m = l2.size();
     }

     if(m < nonDefaults || m > list.size())
     {
        System.out.printf("- Line %s: The function needs %d to %d arguments to be called properly and here there are %d passed (error type 3)%n", line, nonDefaults, list.size(), m);
     }

     else
     {
       AAssignStatement ass = null;

       for (int i=0; i<m; i++)
       {
         if (l2.get(i) instanceof ANumbExpression)
         {
           ANumbExpression num = new ANumbExpression(((ANumbExpression)l2.get(i)).getNumber());
           Object obj = ((AArgArgument)list.get(i)).clone();
           AArgArgument arg = ((AArgArgument)obj);
           TId t = arg.getId();
           ass = new AAssignStatement(t, num);
           String name = (t.toString());
           symtable.put(name, ass);
         }
         else if (l2.get(i) instanceof AStringdoubleqExpression)
         {
           AStringdoubleqExpression strd = new AStringdoubleqExpression(((AStringdoubleqExpression)l2.get(i)).getStringdoubleq());
           Object obj = ((AArgArgument)list.get(i)).clone();
           AArgArgument arg = ((AArgArgument)obj);
           TId t = arg.getId();
           ass = new AAssignStatement(t, strd);
           String name = (t.toString());

           symtable.put(name, ass);

         }
         else if (l2.get(i) instanceof AStringsingleqExpression)
         {
           AStringsingleqExpression strs = new AStringsingleqExpression(((AStringsingleqExpression)l2.get(i)).getStringsingleq());
           Object obj = ((AArgArgument)list.get(i)).clone();
           AArgArgument arg = ((AArgArgument)obj);
           TId t = arg.getId();
           ass = new AAssignStatement(t, strs);
           String name = (t.toString());

           symtable.put(name, ass);
         }
       }

         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof AAdditionExpression)
         {
           AAdditionExpression ade = new AAdditionExpression(((AAdditionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((AAdditionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inAAdditionExpression(ade);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof ASubtractionExpression)
         {
           ASubtractionExpression sube = new ASubtractionExpression(((ASubtractionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((ASubtractionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inASubtractionExpression(sube);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof AMultiplicationExpression)
         {
           AMultiplicationExpression multe = new AMultiplicationExpression(((AMultiplicationExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((AMultiplicationExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
           inAMultiplicationExpression(multe);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof ADivisionExpression)
         {
           ADivisionExpression dive = new ADivisionExpression(((ADivisionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((ADivisionExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inADivisionExpression(dive);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof AModExpression)
         {
           AModExpression mode = new AModExpression(((AModExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((AModExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inAModExpression(mode);
         }
         if (((AReturnStatement)((AFuncFunction) n).getStatement()).getExpression() instanceof APowerExpression)
         {
           APowerExpression powe = new APowerExpression(((APowerExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getL()
                                                            ,((APowerExpression)(((AReturnStatement)((AFuncFunction) n).getStatement())).getExpression()).getR());
            inAPowerExpression(powe);
         }

       symtable.put(fName+"call", node);
     }
   }
 }

 //error type 1
   public void inAAssignStatement(AAssignStatement node)
     {
       String fName = node.getId().toString();
       if(!symtable.containsKey(fName))
       {
         symtable.put(fName, node);
       }
     }

//error type 1
	public void inAArgArgument(AArgArgument node)
	{
    String fName = node.getId().toString();
    if(!symtable.containsKey(fName))
    {
      symtable.put(fName, node);
    }
	}

//error type 1
  public void inAIdentifierExpression(AIdentifierExpression node)
  {
    String fName = node.getId().toString();
    int line = ((TId) node.getId()).getLine();
    int pos = ((TId) node.getId()).getPos();

    if (!symtable.containsKey(fName))
    {
      System.out.printf("- Line %d: Pos %d: Identifier %s has not been declared (error type 1)%n", line, pos, fName);
    }
  }
  //error type 4,5,6
  public void inAAdditionExpression(AAdditionExpression node)
  {
    String fName = node.getL().toString();
    String fName2 = node.getR().toString();
    int line;
    int pos;

    if(fName.equals("none "))
    {

      line = ((ANoneExpression) node.getL()).getNone().getLine();
      pos = ((ANoneExpression) node.getL()).getNone().getPos();
      System.out.printf("- Line %d: Pos %d: Can't execute an addition with a none operand (error type 5)%n", line, pos);
    }
    if(fName2.equals("none "))
    {
      line = ((ANoneExpression) node.getR()).getNone().getLine();
      pos = ((ANoneExpression) node.getR()).getNone().getPos();
      System.out.printf("- Line %d: Pos %d: Can't execute an addition with a none operand (error type 5)%n", line, pos);
    }

    String s1 = node.getL().getClass().toString();
    String s2 = node.getR().getClass().toString();

    //error type 6
    if (s1.equals("class minipython.node.AFunctioncallExpression"))
    {
      String name = ((AFunctioncallExpression)node.getL()).getId().toString();
      Object n1 = symtable.get(name);
      s1 = ((AReturnStatement)((AFuncFunction) n1).getStatement()).getExpression().getClass().toString();
    }
    if (s1.equals("class minipython.node.AFunctioncalldotExpression"))
    {
      String name = ((AFunctioncalldotExpression)node.getL()).getR().toString();
      Object n1 = symtable.get(name);
      s1 = ((AReturnStatement)((AFuncFunction) n1).getStatement()).getExpression().getClass().toString();
    }
    //error type 6
    if (s2.equals("class minipython.node.AFunctioncallExpression"))
    {
      String name2 = ((AFunctioncallExpression)node.getR()).getId().toString();
      Object n2 = symtable.get(name2);
      s2 = ((AReturnStatement)((AFuncFunction) n2).getStatement()).getExpression().getClass().toString();
    }
    if (s2.equals("class minipython.node.AFunctioncalldotExpression"))
    {
      String name2 = ((AFunctioncalldotExpression)node.getR()).getR().toString();
      Object n2 = symtable.get(name2);
      s2 = ((AReturnStatement)((AFuncFunction) n2).getStatement()).getExpression().getClass().toString();
    }

    if (s1.equals("class minipython.node.AIdentifierExpression"))
    {
      Object n1 = symtable.get(fName);
      if (n1 instanceof AAssignStatement)
      {
        s1 = (((AAssignStatement) n1).getExpression().getClass().toString());
      }
    }

    if (s2.equals("class minipython.node.AIdentifierExpression"))
    {
      Object n2 = symtable.get(fName2);
      if (n2 instanceof AAssignStatement)
      {
        s2 = (((AAssignStatement) n2).getExpression().getClass().toString());
      }
    }

    if (s1.equals("class minipython.node.AAdditionExpression"))
    {
      Object obj =  node.getL().clone();
      AAdditionExpression a = ((AAdditionExpression)obj);
      inAAdditionExpression(a);
    }
    if (s2.equals("class minipython.node.AAdditionExpression"))
    {
      Object obj =  node.getR().clone();
      AAdditionExpression a = ((AAdditionExpression)obj);
      inAAdditionExpression(a);
    }
    if (s1.equals("class minipython.node.ASubtractionExpression"))
    {
      Object obj =  node.getL().clone();
      ASubtractionExpression a = ((ASubtractionExpression)obj);
      inASubtractionExpression(a);
    }
    if (s2.equals("class minipython.node.ASubtractionExpression"))
    {
      Object obj =  node.getR().clone();
      ASubtractionExpression a = ((ASubtractionExpression)obj);
      inASubtractionExpression(a);
    }
    if (s1.equals("class minipython.node.AMultiplicationExpression"))
    {
      Object obj =  node.getL().clone();
      AMultiplicationExpression a = ((AMultiplicationExpression)obj);
      inAMultiplicationExpression(a);
    }
    if (s2.equals("class minipython.node.AMultiplicationExpression"))
    {
      Object obj =  node.getR().clone();
      AMultiplicationExpression a = ((AMultiplicationExpression)obj);
      inAMultiplicationExpression(a);
    }
    if (s1.equals("class minipython.node.ADivisionExpression"))
    {
      Object obj =  node.getL().clone();
      ADivisionExpression a = ((ADivisionExpression)obj);
      inADivisionExpression(a);
    }
    if (s2.equals("class minipython.node.ADivisionExpression"))
    {
      Object obj =  node.getR().clone();
      ADivisionExpression a = ((ADivisionExpression)obj);
      inADivisionExpression(a);
    }
    if (s1.equals("class minipython.node.AModExpression"))
    {
      Object obj =  node.getL().clone();
      AModExpression a = ((AModExpression)obj);
      inAModExpression(a);
    }
    if (s2.equals("class minipython.node.AModExpression"))
    {
      Object obj =  node.getR().clone();
      AModExpression a = ((AModExpression)obj);
      inAModExpression(a);
    }
    if (s1.equals("class minipython.node.APowerExpression"))
    {
      Object obj =  node.getL().clone();
      APowerExpression a = ((APowerExpression)obj);
      inAPowerExpression(a);
    }
    if (s2.equals("class minipython.node.APowerExpression"))
    {
      Object obj =  node.getR().clone();
      APowerExpression a = ((APowerExpression)obj);
      inAPowerExpression(a);
    }

    if (s1.equals("class minipython.node.AStringdoubleqExpression") && !s2.equals("class minipython.node.AStringdoubleqExpression"))
    {
      if (node.getL() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getL()).getId().getLine();
        pos = ((AIdentifierExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Addition with a String (error type 4)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getL()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Addition with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getL()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getL()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Addition with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else
      {
        line = ((AStringdoubleqExpression) node.getL()).getStringdoubleq().getLine();
        pos = ((AStringdoubleqExpression) node.getL()).getStringdoubleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Addition with a String (error type 4)%n", line, pos);
      }
    }
    if (s1.equals("class minipython.node.AStringsingleqExpression") && !s2.equals("class minipython.node.AStringsingleqExpression"))
    {
      if (node.getL() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getL()).getId().getLine();
        pos = ((AIdentifierExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Addition with a String (error type 4)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getL()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Addition with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getL()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getL()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Addition with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else
      {
        line = ((AStringsingleqExpression) node.getL()).getStringsingleq().getLine();
        pos = ((AStringsingleqExpression) node.getL()).getStringsingleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Addition with a String (error type 4)%n", line, pos);
      }
    }
    if (s1.equals("class minipython.node.ANumbExpression") && !s2.equals("class minipython.node.ANumbExpression"))
    {
      if (node.getL() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getL()).getId().getLine();
        pos = ((AIdentifierExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Addition with a String (error type 4)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getL()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Addition with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getL()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getL()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Addition with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else
      {
        line = ((ANumbExpression) node.getL()).getNumber().getLine();
        pos = ((ANumbExpression) node.getL()).getNumber().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Addition with a String (error type 4)%n", line, pos);
      }
    }
  }

//error type 4,5,6
  public void inASubtractionExpression(ASubtractionExpression node)
  {
    String fName = node.getL().toString();
    String fName2 = node.getR().toString();
    int line;
    int pos;

    if(fName.equals("none "))
    {

      line = ((ANoneExpression) node.getL()).getNone().getLine();
      pos = ((ANoneExpression) node.getL()).getNone().getPos();
      System.out.printf("- Line %d: Pos %d: Can't execute a subtraction with a none operand (error type 5)%n", line, pos);
    }
    if(fName2.equals("none "))
    {
      line = ((ANoneExpression) node.getR()).getNone().getLine();
      pos = ((ANoneExpression) node.getR()).getNone().getPos();
      System.out.printf("- Line %d: Pos %d: Can't execute a subtraction with a none operand (error type 5)%n", line, pos);
    }

    String s1 = node.getL().getClass().toString();
    String s2 = node.getR().getClass().toString();

    if (s1.equals("class minipython.node.AFunctioncallExpression"))
    {
      String name = ((AFunctioncallExpression)node.getL()).getId().toString();
      Object n1 = symtable.get(name);
      s1 = ((AReturnStatement)((AFuncFunction) n1).getStatement()).getExpression().getClass().toString();
    }
    if (s1.equals("class minipython.node.AFunctioncalldotExpression"))
    {
      String name = ((AFunctioncalldotExpression)node.getL()).getR().toString();
      Object n1 = symtable.get(name);
      s1 = ((AReturnStatement)((AFuncFunction) n1).getStatement()).getExpression().getClass().toString();
    }
    //error type 6
    if (s2.equals("class minipython.node.AFunctioncallExpression"))
    {
      String name2 = ((AFunctioncallExpression)node.getR()).getId().toString();
      Object n2 = symtable.get(name2);
      s2 = ((AReturnStatement)((AFuncFunction) n2).getStatement()).getExpression().getClass().toString();
    }
    if (s2.equals("class minipython.node.AFunctioncalldotExpression"))
    {
      String name2 = ((AFunctioncalldotExpression)node.getR()).getR().toString();
      Object n2 = symtable.get(name2);
      s2 = ((AReturnStatement)((AFuncFunction) n2).getStatement()).getExpression().getClass().toString();
    }

    if (s1.equals("class minipython.node.AIdentifierExpression"))
    {
      Object n1 = symtable.get(fName);
      if (n1 instanceof AAssignStatement)
      {
        s1 = (((AAssignStatement) n1).getExpression().getClass().toString());
      }
    }
    if (s2.equals("class minipython.node.AIdentifierExpression"))
    {
      Object n2 = symtable.get(fName2);
      if (n2 instanceof AAssignStatement)
      {
        s2 = (((AAssignStatement) n2).getExpression().getClass().toString());
      }
    }

    if (s1.equals("class minipython.node.AAdditionExpression"))
    {
      Object obj =  node.getL().clone();
      AAdditionExpression a = ((AAdditionExpression)obj);
      inAAdditionExpression(a);
    }
    if (s2.equals("class minipython.node.AAdditionExpression"))
    {
      Object obj =  node.getR().clone();
      AAdditionExpression a = ((AAdditionExpression)obj);
      inAAdditionExpression(a);
    }
    if (s1.equals("class minipython.node.ASubtractionExpression"))
    {
      Object obj =  node.getL().clone();
      ASubtractionExpression a = ((ASubtractionExpression)obj);
      inASubtractionExpression(a);
    }
    if (s2.equals("class minipython.node.ASubtractionExpression"))
    {
      Object obj =  node.getR().clone();
      ASubtractionExpression a = ((ASubtractionExpression)obj);
      inASubtractionExpression(a);
    }
    if (s1.equals("class minipython.node.AMultiplicationExpression"))
    {
      Object obj =  node.getL().clone();
      AMultiplicationExpression a = ((AMultiplicationExpression)obj);
      inAMultiplicationExpression(a);
    }
    if (s2.equals("class minipython.node.AMultiplicationExpression"))
    {
      Object obj =  node.getR().clone();
      AMultiplicationExpression a = ((AMultiplicationExpression)obj);
      inAMultiplicationExpression(a);
    }
    if (s1.equals("class minipython.node.ADivisionExpression"))
    {
      Object obj =  node.getL().clone();
      ADivisionExpression a = ((ADivisionExpression)obj);
      inADivisionExpression(a);
    }
    if (s2.equals("class minipython.node.ADivisionExpression"))
    {
      Object obj =  node.getR().clone();
      ADivisionExpression a = ((ADivisionExpression)obj);
      inADivisionExpression(a);
    }
    if (s1.equals("class minipython.node.AModExpression"))
    {
      Object obj =  node.getL().clone();
      AModExpression a = ((AModExpression)obj);
      inAModExpression(a);
    }
    if (s2.equals("class minipython.node.AModExpression"))
    {
      Object obj =  node.getR().clone();
      AModExpression a = ((AModExpression)obj);
      inAModExpression(a);
    }
    if (s1.equals("class minipython.node.APowerExpression"))
    {
      Object obj =  node.getL().clone();
      APowerExpression a = ((APowerExpression)obj);
      inAPowerExpression(a);
    }
    if (s2.equals("class minipython.node.APowerExpression"))
    {
      Object obj =  node.getR().clone();
      APowerExpression a = ((APowerExpression)obj);
      inAPowerExpression(a);
    }

    if (s1.equals("class minipython.node.AStringdoubleqExpression") || s1.equals("class minipython.node.AStringsingleqExpression"))
    {
      if (node.getL() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getL()).getId().getLine();
        pos = ((AIdentifierExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Subtraction with a String (error type 4)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getL()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Subtraction with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getL()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getL()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Subtraction with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AStringdoubleqExpression)
      {
        line = ((AStringdoubleqExpression) node.getL()).getStringdoubleq().getLine();
        pos = ((AStringdoubleqExpression) node.getL()).getStringdoubleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Subtraction with a String (error type 4)%n", line, pos);
      }
      else
      {
        line = ((AStringsingleqExpression) node.getL()).getStringsingleq().getLine();
        pos = ((AStringsingleqExpression) node.getL()).getStringsingleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Subtraction with a String (error type 4)%n", line, pos);
      }
    }
    if (s2.equals("class minipython.node.AStringdoubleqExpression") || s2.equals("class minipython.node.AStringsingleqExpression"))
    {
      if (node.getR() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getR()).getId().getLine();
        pos = ((AIdentifierExpression) node.getR()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Subtraction with a String (error type 4)%n", line, pos);
      }
      else if (node.getR() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getR()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getR()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Subtraction with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getR() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getR()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getR()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Subtraction with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getR() instanceof AStringdoubleqExpression)
      {
        line = ((AStringdoubleqExpression) node.getR()).getStringdoubleq().getLine();
        pos = ((AStringdoubleqExpression) node.getR()).getStringdoubleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Subtraction with a String (error type 4)%n", line, pos);
      }
      else
      {
        line = ((AStringsingleqExpression) node.getR()).getStringsingleq().getLine();
        pos = ((AStringsingleqExpression) node.getR()).getStringsingleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Subtraction with a String (error type 4)%n", line, pos);
      }
    }
  }
//error type 4,5,6
  public void inAMultiplicationExpression(AMultiplicationExpression node)
  {
    String fName = node.getL().toString();
    String fName2 = node.getR().toString();
    int line;
    int pos;

    if(fName.equals("none "))
    {

      line = ((ANoneExpression) node.getL()).getNone().getLine();
      pos = ((ANoneExpression) node.getL()).getNone().getPos();
      System.out.printf("- Line %d: Pos %d: Can't execute a multplication with a none operand (error type 5)%n", line, pos);
    }
    if(fName2.equals("none "))
    {
      line = ((ANoneExpression) node.getR()).getNone().getLine();
      pos = ((ANoneExpression) node.getR()).getNone().getPos();
      System.out.printf("- Line %d: Pos %d: Can't execute a multplication with a none operand (error type 5)%n", line, pos);
    }

    String s1 = node.getL().getClass().toString();
    String s2 = node.getR().getClass().toString();

    if (s1.equals("class minipython.node.AFunctioncallExpression"))
    {
      String name = ((AFunctioncallExpression)node.getL()).getId().toString();
      Object n1 = symtable.get(name);
      s1 = ((AReturnStatement)((AFuncFunction) n1).getStatement()).getExpression().getClass().toString();
    }
    if (s1.equals("class minipython.node.AFunctioncalldotExpression"))
    {
      String name = ((AFunctioncalldotExpression)node.getL()).getR().toString();
      Object n1 = symtable.get(name);
      s1 = ((AReturnStatement)((AFuncFunction) n1).getStatement()).getExpression().getClass().toString();
    }
    //error type 6
    if (s2.equals("class minipython.node.AFunctioncallExpression"))
    {
      String name2 = ((AFunctioncallExpression)node.getR()).getId().toString();
      Object n2 = symtable.get(name2);
      s2 = ((AReturnStatement)((AFuncFunction) n2).getStatement()).getExpression().getClass().toString();
    }
    if (s2.equals("class minipython.node.AFunctioncalldotExpression"))
    {
      String name2 = ((AFunctioncalldotExpression)node.getR()).getR().toString();
      Object n2 = symtable.get(name2);
      s2 = ((AReturnStatement)((AFuncFunction) n2).getStatement()).getExpression().getClass().toString();
    }

    if (s1.equals("class minipython.node.AIdentifierExpression"))
    {
      Object n1 = symtable.get(fName);
      if (n1 instanceof AAssignStatement)
      {
        s1 = (((AAssignStatement) n1).getExpression().getClass().toString());
      }
    }
    if (s2.equals("class minipython.node.AIdentifierExpression"))
    {
      Object n2 = symtable.get(fName2);
      if (n2 instanceof AAssignStatement)
      {
        s2 = (((AAssignStatement) n2).getExpression().getClass().toString());
      }
    }

    if (s1.equals("class minipython.node.AAdditionExpression"))
    {
      Object obj =  node.getL().clone();
      AAdditionExpression a = ((AAdditionExpression)obj);
      inAAdditionExpression(a);
    }
    if (s2.equals("class minipython.node.AAdditionExpression"))
    {
      Object obj =  node.getR().clone();
      AAdditionExpression a = ((AAdditionExpression)obj);
      inAAdditionExpression(a);
    }
    if (s1.equals("class minipython.node.ASubtractionExpression"))
    {
      Object obj =  node.getL().clone();
      ASubtractionExpression a = ((ASubtractionExpression)obj);
      inASubtractionExpression(a);
    }
    if (s2.equals("class minipython.node.ASubtractionExpression"))
    {
      Object obj =  node.getR().clone();
      ASubtractionExpression a = ((ASubtractionExpression)obj);
      inASubtractionExpression(a);
    }
    if (s1.equals("class minipython.node.AMultiplicationExpression"))
    {
      Object obj =  node.getL().clone();
      AMultiplicationExpression a = ((AMultiplicationExpression)obj);
      inAMultiplicationExpression(a);
    }
    if (s2.equals("class minipython.node.AMultiplicationExpression"))
    {
      Object obj =  node.getR().clone();
      AMultiplicationExpression a = ((AMultiplicationExpression)obj);
      inAMultiplicationExpression(a);
    }
    if (s1.equals("class minipython.node.ADivisionExpression"))
    {
      Object obj =  node.getL().clone();
      ADivisionExpression a = ((ADivisionExpression)obj);
      inADivisionExpression(a);
    }
    if (s2.equals("class minipython.node.ADivisionExpression"))
    {
      Object obj =  node.getR().clone();
      ADivisionExpression a = ((ADivisionExpression)obj);
      inADivisionExpression(a);
    }
    if (s1.equals("class minipython.node.AModExpression"))
    {
      Object obj =  node.getL().clone();
      AModExpression a = ((AModExpression)obj);
      inAModExpression(a);
    }
    if (s2.equals("class minipython.node.AModExpression"))
    {
      Object obj =  node.getR().clone();
      AModExpression a = ((AModExpression)obj);
      inAModExpression(a);
    }
    if (s1.equals("class minipython.node.APowerExpression"))
    {
      Object obj =  node.getL().clone();
      APowerExpression a = ((APowerExpression)obj);
      inAPowerExpression(a);
    }
    if (s2.equals("class minipython.node.APowerExpression"))
    {
      Object obj =  node.getR().clone();
      APowerExpression a = ((APowerExpression)obj);
      inAPowerExpression(a);
    }

    if (s1.equals("class minipython.node.AStringdoubleqExpression") || s1.equals("class minipython.node.AStringsingleqExpression"))
    {
      if (node.getL() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getL()).getId().getLine();
        pos = ((AIdentifierExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Multiplication with a String (error type 4)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getL()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Multiplication with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getL()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getL()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Multiplication with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AStringdoubleqExpression)
      {
        line = ((AStringdoubleqExpression) node.getL()).getStringdoubleq().getLine();
        pos = ((AStringdoubleqExpression) node.getL()).getStringdoubleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Multiplication with a String (error type 4)%n", line, pos);
      }
      else
      {
        line = ((AStringsingleqExpression) node.getL()).getStringsingleq().getLine();
        pos = ((AStringsingleqExpression) node.getL()).getStringsingleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Multiplication with a String (error type 4)%n", line, pos);
      }
    }
    if (s2.equals("class minipython.node.AStringdoubleqExpression") || s2.equals("class minipython.node.AStringsingleqExpression"))
    {
      if (node.getR() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getR()).getId().getLine();
        pos = ((AIdentifierExpression) node.getR()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Multiplication with a String (error type 4)%n", line, pos);
      }
      else if (node.getR() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getR()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getR()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Multiplication with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getR() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getR()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getR()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Multiplication with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getR() instanceof AStringdoubleqExpression)
      {
        line = ((AStringdoubleqExpression) node.getR()).getStringdoubleq().getLine();
        pos = ((AStringdoubleqExpression) node.getR()).getStringdoubleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Multiplication with a String (error type 4)%n", line, pos);
      }
      else
      {
        line = ((AStringsingleqExpression) node.getR()).getStringsingleq().getLine();
        pos = ((AStringsingleqExpression) node.getR()).getStringsingleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Multiplication with a String (error type 4)%n", line, pos);
      }
    }
  }
//error type 4,5,6
  public void inADivisionExpression(ADivisionExpression node)
  {
    String fName = node.getL().toString();
    String fName2 = node.getR().toString();
    int line;
    int pos;

    if(fName.equals("none "))
    {

      line = ((ANoneExpression) node.getL()).getNone().getLine();
      pos = ((ANoneExpression) node.getL()).getNone().getPos();
      System.out.printf("- Line %d: Pos %d: Can't execute a division with a none operand (error type 5)%n", line, pos);
    }
    if(fName2.equals("none "))
    {
      line = ((ANoneExpression) node.getR()).getNone().getLine();
      pos = ((ANoneExpression) node.getR()).getNone().getPos();
      System.out.printf("- Line %d: Pos %d: Can't execute a division with a none operand (error type 5)%n", line, pos);
    }

    String s1 = node.getL().getClass().toString();
    String s2 = node.getR().getClass().toString();

    if (s1.equals("class minipython.node.AFunctioncallExpression"))
    {
      String name = ((AFunctioncallExpression)node.getL()).getId().toString();
      Object n1 = symtable.get(name);
      s1 = ((AReturnStatement)((AFuncFunction) n1).getStatement()).getExpression().getClass().toString();
    }
    if (s1.equals("class minipython.node.AFunctioncalldotExpression"))
    {
      String name = ((AFunctioncalldotExpression)node.getL()).getR().toString();
      Object n1 = symtable.get(name);
      s1 = ((AReturnStatement)((AFuncFunction) n1).getStatement()).getExpression().getClass().toString();
    }
    //error type 6
    if (s2.equals("class minipython.node.AFunctioncallExpression"))
    {
      String name2 = ((AFunctioncallExpression)node.getR()).getId().toString();
      Object n2 = symtable.get(name2);
      s2 = ((AReturnStatement)((AFuncFunction) n2).getStatement()).getExpression().getClass().toString();
    }
    if (s2.equals("class minipython.node.AFunctioncalldotExpression"))
    {
      String name2 = ((AFunctioncalldotExpression)node.getR()).getR().toString();
      Object n2 = symtable.get(name2);
      s2 = ((AReturnStatement)((AFuncFunction) n2).getStatement()).getExpression().getClass().toString();
    }


    if (s1.equals("class minipython.node.AIdentifierExpression"))
    {
      Object n1 = symtable.get(fName);
      if (n1 instanceof AAssignStatement)
      {
        s1 = (((AAssignStatement) n1).getExpression().getClass().toString());
      }
    }
    if (s2.equals("class minipython.node.AIdentifierExpression"))
    {
      Object n2 = symtable.get(fName2);
      if (n2 instanceof AAssignStatement)
      {
        s2 = (((AAssignStatement) n2).getExpression().getClass().toString());
      }
    }

    if (s1.equals("class minipython.node.AAdditionExpression"))
    {
      Object obj =  node.getL().clone();
      AAdditionExpression a = ((AAdditionExpression)obj);
      inAAdditionExpression(a);
    }
    if (s2.equals("class minipython.node.AAdditionExpression"))
    {
      Object obj =  node.getR().clone();
      AAdditionExpression a = ((AAdditionExpression)obj);
      inAAdditionExpression(a);
    }
    if (s1.equals("class minipython.node.ASubtractionExpression"))
    {
      Object obj =  node.getL().clone();
      ASubtractionExpression a = ((ASubtractionExpression)obj);
      inASubtractionExpression(a);
    }
    if (s2.equals("class minipython.node.ASubtractionExpression"))
    {
      Object obj =  node.getR().clone();
      ASubtractionExpression a = ((ASubtractionExpression)obj);
      inASubtractionExpression(a);
    }
    if (s1.equals("class minipython.node.AMultiplicationExpression"))
    {
      Object obj =  node.getL().clone();
      AMultiplicationExpression a = ((AMultiplicationExpression)obj);
      inAMultiplicationExpression(a);
    }
    if (s2.equals("class minipython.node.AMultiplicationExpression"))
    {
      Object obj =  node.getR().clone();
      AMultiplicationExpression a = ((AMultiplicationExpression)obj);
      inAMultiplicationExpression(a);
    }
    if (s1.equals("class minipython.node.ADivisionExpression"))
    {
      Object obj =  node.getL().clone();
      ADivisionExpression a = ((ADivisionExpression)obj);
      inADivisionExpression(a);
    }
    if (s2.equals("class minipython.node.ADivisionExpression"))
    {
      Object obj =  node.getR().clone();
      ADivisionExpression a = ((ADivisionExpression)obj);
      inADivisionExpression(a);
    }
    if (s1.equals("class minipython.node.AModExpression"))
    {
      Object obj =  node.getL().clone();
      AModExpression a = ((AModExpression)obj);
      inAModExpression(a);
    }
    if (s2.equals("class minipython.node.AModExpression"))
    {
      Object obj =  node.getR().clone();
      AModExpression a = ((AModExpression)obj);
      inAModExpression(a);
    }
    if (s1.equals("class minipython.node.APowerExpression"))
    {
      Object obj =  node.getL().clone();
      APowerExpression a = ((APowerExpression)obj);
      inAPowerExpression(a);
    }
    if (s2.equals("class minipython.node.APowerExpression"))
    {
      Object obj =  node.getR().clone();
      APowerExpression a = ((APowerExpression)obj);
      inAPowerExpression(a);
    }

    if (s1.equals("class minipython.node.AStringdoubleqExpression") || s1.equals("class minipython.node.AStringsingleqExpression"))
    {
      if (node.getL() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getL()).getId().getLine();
        pos = ((AIdentifierExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Division with a String (error type 4)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getL()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Division with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getL()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getL()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Division with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AStringdoubleqExpression)
      {
        line = ((AStringdoubleqExpression) node.getL()).getStringdoubleq().getLine();
        pos = ((AStringdoubleqExpression) node.getL()).getStringdoubleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Division with a String (error type 4)%n", line, pos);
      }
      else
      {
        line = ((AStringsingleqExpression) node.getL()).getStringsingleq().getLine();
        pos = ((AStringsingleqExpression) node.getL()).getStringsingleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Division with a String (error type 4)%n", line, pos);
      }
    }
    if (s2.equals("class minipython.node.AStringdoubleqExpression") || s2.equals("class minipython.node.AStringsingleqExpression"))
    {
      if (node.getR() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getR()).getId().getLine();
        pos = ((AIdentifierExpression) node.getR()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Division with a String (error type 4)%n", line, pos);
      }
      else if (node.getR() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getR()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getR()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Division with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getR() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getR()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getR()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Division with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getR() instanceof AStringdoubleqExpression)
      {
        line = ((AStringdoubleqExpression) node.getR()).getStringdoubleq().getLine();
        pos = ((AStringdoubleqExpression) node.getR()).getStringdoubleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Division with a String (error type 4)%n", line, pos);
      }
      else
      {
        line = ((AStringsingleqExpression) node.getR()).getStringsingleq().getLine();
        pos = ((AStringsingleqExpression) node.getR()).getStringsingleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Division with a String (error type 4)%n", line, pos);
      }
    }
  }
//error type 4,5,6
  public void inAModExpression(AModExpression node)
  {
    String fName = node.getL().toString();
    String fName2 = node.getR().toString();
    int line;
    int pos;

    if(fName.equals("none "))
    {

      line = ((ANoneExpression) node.getL()).getNone().getLine();
      pos = ((ANoneExpression) node.getL()).getNone().getPos();
      System.out.printf("- Line %d: Pos %d: Can't execute a modulo operation with a none operand (error type 5)%n", line, pos);
    }
    if(fName2.equals("none "))
    {
      line = ((ANoneExpression) node.getR()).getNone().getLine();
      pos = ((ANoneExpression) node.getR()).getNone().getPos();
      System.out.printf("- Line %d: Pos %d: Can't execute a modulo operation with a none operand (error type 5)%n", line, pos);
    }

    String s1 = node.getL().getClass().toString();
    String s2 = node.getR().getClass().toString();

    if (s1.equals("class minipython.node.AFunctioncallExpression"))
    {
      String name = ((AFunctioncallExpression)node.getL()).getId().toString();
      Object n1 = symtable.get(name);
      s1 = ((AReturnStatement)((AFuncFunction) n1).getStatement()).getExpression().getClass().toString();
    }
    if (s1.equals("class minipython.node.AFunctioncalldotExpression"))
    {
      String name = ((AFunctioncalldotExpression)node.getL()).getR().toString();
      Object n1 = symtable.get(name);
      s1 = ((AReturnStatement)((AFuncFunction) n1).getStatement()).getExpression().getClass().toString();
    }
    //error type 6
    if (s2.equals("class minipython.node.AFunctioncallExpression"))
    {
      String name2 = ((AFunctioncallExpression)node.getR()).getId().toString();
      Object n2 = symtable.get(name2);
      s2 = ((AReturnStatement)((AFuncFunction) n2).getStatement()).getExpression().getClass().toString();
    }
    if (s2.equals("class minipython.node.AFunctioncalldotExpression"))
    {
      String name2 = ((AFunctioncalldotExpression)node.getR()).getR().toString();
      Object n2 = symtable.get(name2);
      s2 = ((AReturnStatement)((AFuncFunction) n2).getStatement()).getExpression().getClass().toString();
    }

    if (s1.equals("class minipython.node.AIdentifierExpression"))
    {
      Object n1 = symtable.get(fName);
      if (n1 instanceof AAssignStatement)
      {
        s1 = (((AAssignStatement) n1).getExpression().getClass().toString());
      }
    }
    if (s2.equals("class minipython.node.AIdentifierExpression"))
    {
      Object n2 = symtable.get(fName2);
      if (n2 instanceof AAssignStatement)
      {
        s2 = (((AAssignStatement) n2).getExpression().getClass().toString());
      }
    }

    if (s1.equals("class minipython.node.AAdditionExpression"))
    {
      Object obj =  node.getL().clone();
      AAdditionExpression a = ((AAdditionExpression)obj);
      inAAdditionExpression(a);
    }
    if (s2.equals("class minipython.node.AAdditionExpression"))
    {
      Object obj =  node.getR().clone();
      AAdditionExpression a = ((AAdditionExpression)obj);
      inAAdditionExpression(a);
    }
    if (s1.equals("class minipython.node.ASubtractionExpression"))
    {
      Object obj =  node.getL().clone();
      ASubtractionExpression a = ((ASubtractionExpression)obj);
      inASubtractionExpression(a);
    }
    if (s2.equals("class minipython.node.ASubtractionExpression"))
    {
      Object obj =  node.getR().clone();
      ASubtractionExpression a = ((ASubtractionExpression)obj);
      inASubtractionExpression(a);
    }
    if (s1.equals("class minipython.node.AMultiplicationExpression"))
    {
      Object obj =  node.getL().clone();
      AMultiplicationExpression a = ((AMultiplicationExpression)obj);
      inAMultiplicationExpression(a);
    }
    if (s2.equals("class minipython.node.AMultiplicationExpression"))
    {
      Object obj =  node.getR().clone();
      AMultiplicationExpression a = ((AMultiplicationExpression)obj);
      inAMultiplicationExpression(a);
    }
    if (s1.equals("class minipython.node.ADivisionExpression"))
    {
      Object obj =  node.getL().clone();
      ADivisionExpression a = ((ADivisionExpression)obj);
      inADivisionExpression(a);
    }
    if (s2.equals("class minipython.node.ADivisionExpression"))
    {
      Object obj =  node.getR().clone();
      ADivisionExpression a = ((ADivisionExpression)obj);
      inADivisionExpression(a);
    }
    if (s1.equals("class minipython.node.AModExpression"))
    {
      Object obj =  node.getL().clone();
      AModExpression a = ((AModExpression)obj);
      inAModExpression(a);
    }
    if (s2.equals("class minipython.node.AModExpression"))
    {
      Object obj =  node.getR().clone();
      AModExpression a = ((AModExpression)obj);
      inAModExpression(a);
    }
    if (s1.equals("class minipython.node.APowerExpression"))
    {
      Object obj =  node.getL().clone();
      APowerExpression a = ((APowerExpression)obj);
      inAPowerExpression(a);
    }
    if (s2.equals("class minipython.node.APowerExpression"))
    {
      Object obj =  node.getR().clone();
      APowerExpression a = ((APowerExpression)obj);
      inAPowerExpression(a);
    }

    if (s1.equals("class minipython.node.AStringdoubleqExpression") || s1.equals("class minipython.node.AStringsingleqExpression"))
    {
      if (node.getL() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getL()).getId().getLine();
        pos = ((AIdentifierExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Modulo Operation with a String (error type 4)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getL()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Modulo Operation with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getL()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getL()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Modulo Operation with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AStringdoubleqExpression)
      {
        line = ((AStringdoubleqExpression) node.getL()).getStringdoubleq().getLine();
        pos = ((AStringdoubleqExpression) node.getL()).getStringdoubleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Modulo Operation with a String (error type 4)%n", line, pos);
      }
      else
      {
        line = ((AStringsingleqExpression) node.getL()).getStringsingleq().getLine();
        pos = ((AStringsingleqExpression) node.getL()).getStringsingleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Modulo Operation with a String (error type 4)%n", line, pos);
      }
    }
    if (s2.equals("class minipython.node.AStringdoubleqExpression") || s2.equals("class minipython.node.AStringsingleqExpression"))
    {
      if (node.getR() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getR()).getId().getLine();
        pos = ((AIdentifierExpression) node.getR()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Modulo Operation with a String (error type 4)%n", line, pos);
      }
      else if (node.getR() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getR()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getR()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Modulo Operation with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getR() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getR()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getR()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Modulo Operation with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getR() instanceof AStringdoubleqExpression)
      {
        line = ((AStringdoubleqExpression) node.getR()).getStringdoubleq().getLine();
        pos = ((AStringdoubleqExpression) node.getR()).getStringdoubleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Modulo Operation with a String (error type 4)%n", line, pos);
      }
      else
      {
        line = ((AStringsingleqExpression) node.getR()).getStringsingleq().getLine();
        pos = ((AStringsingleqExpression) node.getR()).getStringsingleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Modulo Operation with a String (error type 4)%n", line, pos);
      }
    }
  }
//error type 4,5,6
  public void inAPowerExpression(APowerExpression node)
  {
    String fName = node.getL().toString();
    String fName2 = node.getR().toString();
    int line;
    int pos;

    if(fName.equals("none "))
    {

      line = ((ANoneExpression) node.getL()).getNone().getLine();
      pos = ((ANoneExpression) node.getL()).getNone().getPos();
      System.out.printf("- Line %d: Pos %d: Can't execute an exponentiation with a none operand (error type 5)%n", line, pos);
    }
    if(fName2.equals("none "))
    {
      line = ((ANoneExpression) node.getR()).getNone().getLine();
      pos = ((ANoneExpression) node.getR()).getNone().getPos();
      System.out.printf("- Line %d: Pos %d: Can't execute an exponentiation with a none operand (error type 5)%n", line, pos);
    }

    String s1 = node.getL().getClass().toString();
    String s2 = node.getR().getClass().toString();

    if (s1.equals("class minipython.node.AFunctioncallExpression"))
    {
      String name = ((AFunctioncallExpression)node.getL()).getId().toString();
      Object n1 = symtable.get(name);
      s1 = ((AReturnStatement)((AFuncFunction) n1).getStatement()).getExpression().getClass().toString();
    }
    if (s1.equals("class minipython.node.AFunctioncalldotExpression"))
    {
      String name = ((AFunctioncalldotExpression)node.getL()).getR().toString();
      Object n1 = symtable.get(name);
      s1 = ((AReturnStatement)((AFuncFunction) n1).getStatement()).getExpression().getClass().toString();
    }
    //error type 6
    if (s2.equals("class minipython.node.AFunctioncallExpression"))
    {
      String name2 = ((AFunctioncallExpression)node.getR()).getId().toString();
      Object n2 = symtable.get(name2);
      s2 = ((AReturnStatement)((AFuncFunction) n2).getStatement()).getExpression().getClass().toString();
    }
    if (s2.equals("class minipython.node.AFunctioncalldotExpression"))
    {
      String name2 = ((AFunctioncalldotExpression)node.getR()).getR().toString();
      Object n2 = symtable.get(name2);
      s2 = ((AReturnStatement)((AFuncFunction) n2).getStatement()).getExpression().getClass().toString();
    }


    if (s1.equals("class minipython.node.AIdentifierExpression"))
    {
      Object n1 = symtable.get(fName);
      if (n1 instanceof AAssignStatement)
      {
        s1 = (((AAssignStatement) n1).getExpression().getClass().toString());
      }
    }
    if (s2.equals("class minipython.node.AIdentifierExpression"))
    {
      Object n2 = symtable.get(fName2);
      if (n2 instanceof AAssignStatement)
      {
        s2 = (((AAssignStatement) n2).getExpression().getClass().toString());
      }
    }

    if (s1.equals("class minipython.node.AAdditionExpression"))
    {
      Object obj =  node.getL().clone();
      AAdditionExpression a = ((AAdditionExpression)obj);
      inAAdditionExpression(a);
    }
    if (s2.equals("class minipython.node.AAdditionExpression"))
    {
      Object obj =  node.getR().clone();
      AAdditionExpression a = ((AAdditionExpression)obj);
      inAAdditionExpression(a);
    }
    if (s1.equals("class minipython.node.ASubtractionExpression"))
    {
      Object obj =  node.getL().clone();
      ASubtractionExpression a = ((ASubtractionExpression)obj);
      inASubtractionExpression(a);
    }
    if (s2.equals("class minipython.node.ASubtractionExpression"))
    {
      Object obj =  node.getR().clone();
      ASubtractionExpression a = ((ASubtractionExpression)obj);
      inASubtractionExpression(a);
    }
    if (s1.equals("class minipython.node.AMultiplicationExpression"))
    {
      Object obj =  node.getL().clone();
      AMultiplicationExpression a = ((AMultiplicationExpression)obj);
      inAMultiplicationExpression(a);
    }
    if (s2.equals("class minipython.node.AMultiplicationExpression"))
    {
      Object obj =  node.getR().clone();
      AMultiplicationExpression a = ((AMultiplicationExpression)obj);
      inAMultiplicationExpression(a);
    }
    if (s1.equals("class minipython.node.ADivisionExpression"))
    {
      Object obj =  node.getL().clone();
      ADivisionExpression a = ((ADivisionExpression)obj);
      inADivisionExpression(a);
    }
    if (s2.equals("class minipython.node.ADivisionExpression"))
    {
      Object obj =  node.getR().clone();
      ADivisionExpression a = ((ADivisionExpression)obj);
      inADivisionExpression(a);
    }
    if (s1.equals("class minipython.node.AModExpression"))
    {
      Object obj =  node.getL().clone();
      AModExpression a = ((AModExpression)obj);
      inAModExpression(a);
    }
    if (s2.equals("class minipython.node.AModExpression"))
    {
      Object obj =  node.getR().clone();
      AModExpression a = ((AModExpression)obj);
      inAModExpression(a);
    }
    if (s1.equals("class minipython.node.APowerExpression"))
    {
      Object obj =  node.getL().clone();
      APowerExpression a = ((APowerExpression)obj);
      inAPowerExpression(a);
    }
    if (s2.equals("class minipython.node.APowerExpression"))
    {
      Object obj =  node.getR().clone();
      APowerExpression a = ((APowerExpression)obj);
      inAPowerExpression(a);
    }

    if (s1.equals("class minipython.node.AStringdoubleqExpression") || s1.equals("class minipython.node.AStringsingleqExpression"))
    {
      if (node.getL() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getL()).getId().getLine();
        pos = ((AIdentifierExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Exponentiation with a String (error type 4)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getL()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getL()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Exponentiation with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getL()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getL()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Exponentiation with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getL() instanceof AStringdoubleqExpression)
      {
        line = ((AStringdoubleqExpression) node.getL()).getStringdoubleq().getLine();
        pos = ((AStringdoubleqExpression) node.getL()).getStringdoubleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Exponentiation with a String (error type 4)%n", line, pos);
      }
      else
      {
        line = ((AStringsingleqExpression) node.getL()).getStringsingleq().getLine();
        pos = ((AStringsingleqExpression) node.getL()).getStringsingleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Exponentiation with a String (error type 4)%n", line, pos);
      }
    }
    if (s2.equals("class minipython.node.AStringdoubleqExpression") || s2.equals("class minipython.node.AStringsingleqExpression"))
    {
      if (node.getR() instanceof AIdentifierExpression)
      {
        line = ((AIdentifierExpression) node.getR()).getId().getLine();
        pos = ((AIdentifierExpression) node.getR()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Exponentiation with a String (error type 4)%n", line, pos);
      }
      else if (node.getR() instanceof AFunctioncallExpression)
      {
        line = ((AFunctioncallExpression) node.getR()).getId().getLine();
        pos = ((AFunctioncallExpression) node.getR()).getId().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Exponentiation with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getR() instanceof AFunctioncalldotExpression)
      {
        line = ((AFunctioncalldotExpression) node.getR()).getL().getLine();
        pos = ((AFunctioncalldotExpression) node.getR()).getL().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Exponentiation with a String as a Function Return Statement (error type 6)%n", line, pos);
      }
      else if (node.getR() instanceof AStringdoubleqExpression)
      {
        line = ((AStringdoubleqExpression) node.getR()).getStringdoubleq().getLine();
        pos = ((AStringdoubleqExpression) node.getR()).getStringdoubleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Exponentiation with a String (error type 4)%n", line, pos);
      }
      else
      {
        line = ((AStringsingleqExpression) node.getR()).getStringsingleq().getLine();
        pos = ((AStringsingleqExpression) node.getR()).getStringsingleq().getPos();
        System.out.printf("- Line %d: Pos %d: Operands don't match; Exponentiation with a String (error type 4)%n", line, pos);
      }
    }

  }

}
