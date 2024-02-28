Approach:
1. Parse argumnets from Command Line and Connect with server through a SSL socket
2. Send HELLO message from client to server to activate server
3. Receive message from server, calculate result locally and send back to server
4. Repeat Step3 until receiving a BYE message 
5. Parse BYE message to get secret flag

Challenges:
1. Java doesn't have long long data type, to deal with large number, a BigInteger class is used.
2. Java doesn't support built-in floor division, have to rewrite a new method to compute floor division
3. Parsing and computing a long string equation requires some algorithm design.

Test:
1. Create a dummy message and use this dummy message to test whether algorithm works properly.
2. Print each message received from server to make sure message received from server is complete.
3. Set breakpoints at possible lines to check the returned value.



