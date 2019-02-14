/* This is a simple implementation of a client that connects to a server
 * in order to create a "chatroom". This model is fairly simplistic because
 * there exists only one room for everyone to particpate in. To start an
 * instance of a client a user must provide 3 command line arguments when
 * executing the program. The first is the name of the user, which is assumed
 * to be unique among the users. You need not handle the case where a user
 * with the same name already exists in the room when you join.
 * The second command line argument will be the IP v4 address at which the server 
 * can be found. This should be supplied in the form a.b.c.d where a, b, c, and d 
 * are all integers between 0 and 255. If you wish the run the server on your local
 * machine you can use the address 127.0.0.1. In fact you will find that
 * as a result of NATs, the solution to address depreciation problem in IP v4
 * you cannot host a server using many computers as they will have a shared IP
 * address. Lastly you need to supply a port number at which to find the server. 
 * This should be shared with the value the server is called. Since most low port
 * numbers are reserved you will want to choose a large random number in the
 * neightborhood of say 10000. 
 *  Author: Nick Riasanovsky */

#ifndef CLIENT_H
#define CLIENT_H

#ifndef fd_t
#define fd_t int
#endif
extern fd_t socket_fd;

/* Takes in a username, hostname, and port and establishes a connection to
 * a server. The program will first connect to the server and then send
 * the username followed by a newline to the server to allow the server
 * to initialize user information. Then the socket will be set to nonblocking
 * to allow for cycling between it and stdin. If at any point the action is
 * not successful the client will exit. */
void establish_connection (char *username, char *hostname, short port);

/* Function that sets stdin to nonblocking so that attempts to read from it
 * can fail without perminantly waiting on input. Also sets up a user's
 * display message. */
void setup_input ();

/* Function that handles the life of the client. Client will switch between
 * checking from input from the user and checking for a response from the
 * server. */
void maintain_connection ();

/* Function used to handle messages being received from the server. It will
 * read in as much information as is available from the server and will then
 * process the message when a full message is successfully read. A message is
 * complete once a \n is found. */
void receive_messages ();

/* Function that finds the index at which the newline character exists in the
 * message. Returns -1 if no newline exists in the string. */
int find_message_end ();

#endif
