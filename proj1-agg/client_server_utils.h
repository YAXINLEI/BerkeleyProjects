/* File of shared functions between the server and client.
 * Author: Nick Riasanovsky */

#ifndef CLIENT_SERVER_UTILS_H
#define CLIENT_SERVER_UTILS_H

#define MAX_MESSAGE_LENGTH 1025

#ifndef fd_t
#define fd_t int
#endif

enum MESSAGE_TYPE {Standard_Message=1, Exit_Message=2};

/* Function that finds the index at which the newline character exists in the
 * message. Returns -1 if no newline exists in the string. */
int find_message_end (char *msg, int start);

/* Function that takes a char * containing 1 or messages and mallocs and
 * outputs a new char * consisting of the full message of length end. 
 * Also updates messages to hold only the characters following the message. */
char *generate_message (char *messages, int end);

/* Function that terminates the program when a malloc fails. */
void allocation_failed ();

#endif
