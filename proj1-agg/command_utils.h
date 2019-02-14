/* File which contains functions to assist implementation of the
 * various commands. 
 * Author: Nick Riasanovsky */

#ifndef COMMAND_UTILS_H
#define COMMAND_UTILS_H

#include "user_utils.h"

/* Function to determine if a message is a command. */
bool iscommand (char *message);

/* Function to determine if a name is the name of a command. */
bool isknowncommand (char *name);

/* Function that takes in a name and determines if it can be selected as a name
 * for a nickname or a name. */
bool isvalidname (char *name);

/* Function that sorts the Users based upon alphabetical order of names. This 
 * function is called in coordination with the show_all_statuses command. Since
 * this is called by a specific user in location n, n needs to be updated to
 * ensure the correct user receives the message. */
void sort_users (unsigned *n);

/* A simple quicksort implementation to assist sorting users. */
void quick_sort (int lower, int upper, unsigned *n);

/* Function to update the array of users by removing any gaps that developed
 * from users disconnecting. */
void remove_holes (unsigned *n);

/* A swap function to swap all of the information about two
 * users. This will allow for sorting users. */
void swap_information (int a, int b, unsigned *n);

/* Helper function to output the information about user to
 * the user in socket location n. This should be used for
 * the show_status and show_all_statuses commands. */
void output_user_status (struct user_info *user, unsigned n);

#endif
