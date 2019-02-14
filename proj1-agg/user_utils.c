/* File that consists of functions used to handle the information regarding
 * users that have connected to the server.
 * Author: YOUR NAME HERE */

#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include "user_utils.h"
#include "client_server_utils.h"
#include "server.h"

/* * * * * * *READ ME* * * * * * * *
 *                                 *
 * BE SURE AND READ USER_UTILS.H   *
 * FOR THE DEFINITION OF THE       *
 * NAME_INFO AND USER_INFO         *
 * STRUCTS.                        *
 * * * * * * * * * * * * * * * * * */


/* Function that takes in a name and creates a fresh pointer with
 * the name copied over. */
char *create_name (char *name) {
        char *new_pointer = (char*) malloc(strlen(name));
        //char *new_pointer = (char*) malloc(strlen(name)+1);
        strcpy(new_pointer, name);
        return new_pointer;
}


/* Function that takes in a name and outputs a new name_info
 * struct having that name. You cannot assume the name is valid
 * after the function exits. */
struct name_info *create_name_info (char *name) {
	struct name_info *new = malloc(sizeof(struct name_info));
        strcpy(new->name, name);
        return new;
}

/* Function that takes in a name and outputs a new struct user_info having
 * that name. The struct should be capable or producing correct
 * functionality immediately. You cannot assume that name will remain
 * a valid pointer after the function exits. One important detail about
 * the functionality is that while no user begins with a nickname all
 * attempts to communicate between two users involves the sender's
 * nickname. If there is no nickname then this
 * value should be the actual name of the user, but the code given 
 * DOES NOT perform a check to see if there is nickname and then
 * access the name using the name field of the user_info struct. Instead
 * it ALWAYS uses the value given by *(user->nickname), so this must 
 * always contain a valid address whose contents are either the user's
 * nickname or the user's name. */
struct user_info *create_user (char *name) {
	struct user_info *new_user_info = malloc(sizeof(struct user_info));
        struct name_info *new_name_info = malloc(sizeof(struct name_info));
        new_name_info -> name = malloc(strlen(name));
        //char* new_name = malloc(strlen(name)+1);      // allocate space to all the fields needed

        strcpy(new_name_info->name, name);        //copy name into a newly allocated space called new_name
        
        new_user_info -> name_info = new_name_info;   
        new_user_info -> nickname = &(new_name_info->name);

        return new_user_info;
}


/* Function that frees the memory associated with a user. This function
 * should be filled out with some care as it is important that you 
 * DO NOT free any pointers that will be accessed later. It may be useful
 * to implement a system for freeing data only once you know
 * it will no longer be used. The only requirement for when data should
 * be freed is that all data must be freed on a proper server exit. */
void cleanup_user (struct user_info *user) {
	
}

/* Function that frees the memory assoicated with a name info. The same
 * suggestion about freeing memory as the above function applies here as
 * well. */
void cleanup_name_info (struct name_info *info) {
	/* YOUR CODE HERE. */
}



/* Function that takes in a name and determines if it is already a user's
 * name. */
bool istaken_name (char *name) {
        unsigned i = 1;
        unsigned ctr = 1;
        while (i < socket_total) {
                if (sockets[ctr] != -1) {
                        i++;
                        if (strcmp (name, users[ctr]->name_info->name) == 0) {
                                return true;
                        }
                }
                ctr++;
        }
        return false;
}

/* Function that determines if a user has a nickname. */
bool has_nickname (struct user_info *user) {
	if(strcmp(*(user->nickname), user->name_info->name) == 0){
              return false;
        }
        return true;
}


/* Function that takes in a name and determines if a user
 * has it as a nickname. 
 * 
 * You may find the global variables users,
 * socket_total, and sockets helpful here (which are defined as extern
 * in server.h). There are socket_total - 1 clients connected and they
 * can occupy indices starting at one. A client exists at an index i
 * iff sockets[i] != -1. Once you have seen all the clients you should
 * not continue checking later indices. Finally simply because a client
 * is connected does not mean they necessarily have user information
 * yet. If a client is connected at index i but does not have user
 * information yet users[i] will be NULL. */
bool istaken_nickname (char *name) {
	int i = 1; 
        while(i < socket_total){
                
                if (sockets[i] != -1){
                        if(users[i] != NULL && strcmp(*(users[i]->nickname), name) == 0){
                                return true;
                        }
                }

                i++;
        }
        return false;
}

/* Function that takes in a name and return the user who has that name as their
 * actual name. Returns NULL is no user has that actual name. */
struct user_info *find_user (char *name) {
        int start = 1;
        int ctr = 1;
        while (start < socket_total) {
                if (sockets[ctr] != -1) {
                        start++;
                        if (strcmp (users[ctr]->name_info->name, name) == 0) {
                                return users[ctr];
                        }
                }
                ctr++;
        }
        return NULL;
}


/* Takes in a two users and checks if the first user has muted the second
 * user. */
bool ismuted (struct user_info *receiving_user, struct user_info *possibly_muted_user) {
        unsigned i = 0;
        for(i = 0; i < receiving_user->muted_total; i++){
                if (strcmp(receiving_user->muted[i]->name, possibly_muted_user->name_info->name) == 0){
                        return true;
                }
        }
        return false;
}
