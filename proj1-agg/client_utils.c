/* File of utility functions for the client
 * Author: Nick Riasanovsky. */


#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include "client.h"
#include "client_utils.h"
#include "student_client.h"


/* Sends a message to the server. Assumes the input message has been properly
 * formatted by process input. */
void send_message () {
        errno = 0;
        int size;
        int total_length = strlen (input_message);
        int length = 0;
        while (length < total_length) {
                size = write (socket_fd, input_message + length, total_length - length);
                if (size == 0 || (errno && errno != EAGAIN && errno != EWOULDBLOCK)) {
                        handle_server_disconnect ();
                }
                length += size;
        }
}

/* Outputs the "[Me]:" the user should see. */
void display_prefix () {
        printf ("[Me]:");
	fflush (stdout);
}

/* Removes the "[Me]:" so that messages can be properly displayed.
 * Works by resetting the stdout position to the start of the line,
 * then outputing 5 spaces and once again returning the stdout location
 * to the start of the line. */
void clear_prefix () {
        printf ("\r     \r");
	fflush (stdout);
}

/* Function to handle the server having disconnected. It should output
 * "Server has disconnected" and exit properly. Don't forget to clear
 * the prefix. */
void handle_server_disconnect () {
        clear_prefix ();
        printf ("Server has disconnected\n");
        /* Be sure and close your file descriptors. */
        close (socket_fd);
        exit (0);
}
