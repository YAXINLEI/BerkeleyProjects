/* File that contains functions which may be helpful in implementing
 * server functionality. 
 * Author: Nick Riasanovsky */

#ifndef SERVER_UTILS_H
#define SERVER_UTILS_H

/* Function to determine if a character is part of a valid c indentifier,
 * meaning it is a letter (upper or lower case), a number, or an underscore.
 * This function is used to check if the names passed in to commands are
 * valid based on the spec. */
bool isidentifierpart (char c);

/* Function to determine if a char * meets the spec's definition of a word,
 * consisting only of characters which can be part of a c identifier. */
bool isword (char *message);

/* Function that takes in a char ** consisting of count
 * char * (each of which is a message part) and returns a new
 * message which is the result of concatenating all the msgs
 * together after first appending the Standard_Message
 * character to the front (see client_server_utitls.h). */
char *create_message (char **parts, unsigned count);

#endif
