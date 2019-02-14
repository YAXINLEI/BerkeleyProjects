/* A file of functions involving processing messages for students to implement.
 * Author: Nick Riasanovsky. */

#ifndef STUDENT_CLIENT_H
#define STUDENT_CLIENT_H

#define MAX_INPUT_LENGTH 1023

extern char input_message[MAX_INPUT_LENGTH + 1];
extern unsigned input_offset;

/* Processes input from stdin to be written to the server using send_message.
 * If only part of a message has been written then the information should be
 * stored in the input_message array and the next available location stored at
 * input_offset. In particular we will assume that a complete message will
 * never exceed MAX_INPUT_LENGTH and all complete messages will be terminated
 * with a newline character. */
void process_input ();

/* Function that takes in a whole message from a server and handles
 * it appropriately. A message from a server will have a leading byte that
 * indicates what type of message it is. This will either be a
 * Standard_Message or an Exit_Message (see the enum in utils.h). If the
 * message is an Exit_Message the user should call the handle_exit function
 * to exit appropriately. Otherwise the message should be output (without
 * the message type byte). Be sure and remove and readd the "[Me:]" prefix.
 * You may find the clear_prefix and display_prefix helper functions helpful.
 */
void handle_server_message (char *msg);

/* Handles a response from the server telling it to exit. This is the result
 * of the server determining the user has properly issued the \exit command. */
void handle_exit ();

#endif
