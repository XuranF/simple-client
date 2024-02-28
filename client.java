import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/*
    Create a client class to parse arguments passed from command line,
    and client with communicate with server as needed.
 */
public class client {
    public static void main(String[] args) throws IOException {
        client clt=new client();
        clt.parseArgs(args);
        clt.communicate();
    }

    /*
    Arguments needed to create a socket.
     */
    int portNumber;
    String hostName;
    String studentID;

    //1 means column operator's priority is higher than row operator
    //0 means equal
    //-1 means smaller than
    //-256 is just for padding because ')' will never be the peek() element of operator stack
    /*      +   -   *   //  <<^ (   )
        +   >   >   <   <   >   <   >
        -   >   >   <   <   >   <   >
        *   >   >   >   >   >   <   >
        //  >   >   >   >   >   <   >
        <<^ <   <   <   <   >   <   >
        (   <   <   <   <   <   <   ==
        )
     */
    private static final int[][] Priority={
            {1,1,-1,-1,1,-1,1},
            {1,1,-1,-1,1,-1,1},
            {1,1,1,1,1,-1,1},
            {1,1,1,1,1,-1,1},
            {-1,-1,-1,-1,1,-1,1},
            {-1,-1,-1,-1,-1,-1,0},
            {-256,-256,-256,-256,-256,-256,-256}
    };

    // A map is used to map operator string and Priority array index.
    private Map<String,Integer> map=new HashMap<>();

    public client(){
        portNumber=27995;
        hostName="project1.5700.network";
        studentID="";//email address
        map.put("+",0);
        map.put("-",1);
        map.put("*",2);
        map.put("//",3);
        map.put("<<^",4);
        map.put("(",5);
        map.put(")",6);
    }

    /*
    The communication method:
            Step 1: client send a HELLO message to server
            Step 2: while EVAL message is being received by client,
                    client will calculate the result and send back
            Step 3: Step 2 loop exits when BYE message is received,
                    client will print out BYE message.
     */
    private void communicate() throws IOException {
        SSLSocketFactory factory= (SSLSocketFactory) SSLSocketFactory.getDefault();
        Socket socket = factory.createSocket(this.hostName, this.portNumber);
        PrintWriter out=new PrintWriter(socket.getOutputStream());
        BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.println("cs5700fall2022 HELLO " + this.studentID);
        out.flush();

        String s="";
        while(true){
            s=in.readLine();
            if(s.contains("BYE")) {
                System.out.println(s.substring(s.indexOf('E')+2));
                break;
            }

            //Use two stacks to analyze and calculate equation result
            Stack<String> operator=new Stack<>();
            Stack<BigInteger> operand=new Stack<>();
            String msg=s.substring(s.indexOf('L')+2);
            String[] tokens=msg.split(" ");
            boolean isError=false;

            //Loop below is only to do calculation
            for(int i=0;i<tokens.length;){
                String ss=tokens[i];
                //the token is a number
                if(!map.containsKey(ss)) {operand.push(new BigInteger(ss));i++;}
                //the token is an operator
                else{
                    if(operator.size()==0) {operator.push(ss);i++;}
                    else{
                        //Algorithm works like:
                        //      when operator.peek() has a higher priority than current operator,
                        //              pop() twice from operand stack, pop() once from operator stack, do computation and push back
                        //      when operator.peek() has an equal priority as current operator, pop() from operator stack
                        //      when operator.peek() has a lower priority than current operator, push(current operator) into stack
                        int priority=Priority[map.get(operator.peek())][map.get(ss)];
                        if(priority==0) {operator.pop();i++;}
                        else if (priority>0){
                            BigInteger t2=operand.pop();
                            BigInteger t1=operand.pop();
                            if(t2.equals(BigInteger.ZERO)&&operator.peek().equals("//")){
                                out.println("cs5700fall2022 ERR #DIV/0");
                                out.flush();
                                isError=true;
                                break;
                            }
                            operand.push(compute(t1,t2,operator.pop()));
                        }
                        else {operator.push(ss);i++;}
                    }
                }
            }
            if(!isError) out.println("cs5700fall2022 STATUS "+operand.pop());
            out.flush();
        }

        //close all resources
        in.close();
        out.close();
        socket.close();
    }

    /*
    Deal with five different operators and return the computed result
     */
    private BigInteger compute(BigInteger t1, BigInteger t2, String ss){
        int value=map.get(ss);
        switch (value){
            case 0: return t1.add(t2);
            case 1: return t1.subtract(t2);
            case 2: return t1.multiply(t2);
            case 3: {
                //a modified version of getting floor division for BigInteger
                if(t1.signum()*t2.signum()<0){
                    BigInteger[] arr=t1.divideAndRemainder(t2);
                    if(arr[1].signum()==0) return arr[0];
                    return arr[0].subtract(BigInteger.ONE);
                }
                return t1.divide(t2);
            }
            default: return (t1.shiftLeft(13)).xor(t2);
        }
    }

    /*
    Functional method to help with passed arguments from command line
     */
    private void parseArgs(String[] args){
        if(args.length>5||args.length<3) {
            System.out.println("Wrong number of arguments\n");
            System.exit(1);
        }
        if(args.length==3&&!args[0].equals("-s")){
            System.out.println("The first argument should be -s\n");
            System.exit(1);
        }
        
        if(args.length==5){
            this.portNumber=Integer.parseInt(args[1]);
            if(!args[2].equals("-s")){
                System.out.println("The third argument should be -s\n");
                System.exit(1);
            }
        }
        this.hostName=args[args.length-2];
        this.studentID=args[args.length-1];
    }
}
