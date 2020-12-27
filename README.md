# SOCKS5Proxy
This is a SOCKS version 5 proxy.

In the parameters, the program only passes the port on which the proxy will wait for incoming connections from clients.
Of the three commands available in the protocol, one command is implemented - establish a TCP / IP stream connection.
Support for authentication and IPv6 addresses is not implemented.
To implement the proxy, non-blocking sockets are used that work within the same thread. Additional threads are not used. Accordingly, there are no blocking calls (except for calling the selector).
The proxy makes no assumptions about which application layer protocol will be used within the forwarded TCP connection. In particular, data transfer is supported simultaneously in both directions, and connections are closed carefully (only after they are no longer needed).
There are no idle cycles in the application in any situation. In other words, it is not possible for a program state to repeatedly execute a loop body that does not make any actual data transfers per iteration.
There is no unlimited memory consumption for serving one client.
The performance of work through a proxy is not worse than without a proxy. To track the correctness and speed of work, you can look in the Developer tools of the browser at the Network tab.
The proxy supports resolving domain names (value 0x03 in the address field). Resolving is also non-blocking. For this, the following approach is used:
At the start of the program, a new UDP socket is created and added to the read selector
When a domain name needs to be resolved, a DNS request for the A record is sent through this socket to the address of the recursive DNS resolver
The socket read handler handles the case when a response to the DNS request is received, and continues with the received address
To obtain the address of a recursive resolver, as well as to generate and parse DNS messages in Java, the dnsjava library is used.

For testing, you can configure any Web browser to use your proxy, and visit any content-rich website.
