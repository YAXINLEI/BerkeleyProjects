/* File that contains functions which may be helpful in implementing
 * server functionality. 
 * Author: Nick Riasanovsky */

#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <ctype.h>
#include "client_server_utils.h"
#include "server_utils.h"

/* Function to determine if a character is part of a valid c indentifier,
 * meaning it is a letter (upper or lower case), a number, or an underscore.
 * This function is used to check if the names passed in to commands are
 * valid based on the spec. */
bool isidentifierpart (char c) {
        return isalnum (c) || c == '_';
}

/* Function to determine if a char * meets the spec's definition of a word,
 * consisting only of characters which can be part of a c identifier. */
bool isword (char *message) {
        unsigned i = 0;
        while (message[i] != 0) {
                if (!(isidentifierpart (message[i]))) {
                        return false;
                }
                i++;
        }
        return true;
}

/* Function that takes in a char ** consisting of count
 * char * (each of which is a message part) and returns a new
 * message which is the result of concatenating all the msgs
 * together after first appending the Standard_Message
 * character to the front (see client_server_utils.h). */
char *create_message (char **parts, unsigned count) {
        unsigned total_length = 2;
        for (int i = 0; i < count; i++) {
                total_length += strlen (parts[i]);
        }
        char *new_message = malloc (sizeof(char) * total_length);
        if (new_message == NULL) {
                allocation_failed ();
        }
        new_message[0] = Standard_Message;
        new_message[1] = 0;
        for (int i = 0; i < count; i++) {
                strcat (new_message, parts[i]);
        }
        return new_message;
}


