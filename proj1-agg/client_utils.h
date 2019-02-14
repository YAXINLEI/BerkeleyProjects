/* File of utility functions for the client 
 * Author: Nick Riasanovsky */

#ifndef CLIENT_UTILS_H
#define CLIENT_UTILS_H

/* Sends a message to the server. Assumes the input message has been properly
 * formatted by process input. */
void send_message ();

/* Outputs the "[Me]:" the user should see. */
void display_prefix ();

/* Removes the "[Me]:" so that messages can be properly displayed.
 * Works by resetting the stdout position to the start of the line,
 * then outputing 3 spaces and once again returning the stdout location
 * to the start of the line. */
void clear_prefix ();

/* Function to handle the server having disconnected. It should output
 * "Server has disconnected" and exit properly. Don't forget to clear
 * the prefix. */
void handle_server_disconnect ();

#endif
