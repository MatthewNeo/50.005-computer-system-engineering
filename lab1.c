#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>
#include <string.h>
#include <ctype.h>
#define MAX_INPUT 256

int main(){ // start main

	char command[100]; // to store users command
	int valid;
	char **history = (char **) malloc(15 * sizeof(char*));  // character array to store history

	int index = 0;  // index for history

	// while loop to keep asking user for more inputs
	while(1){
		
		printf("csh>");
		fgets(command, MAX_INPUT, stdin); //take input from user
		//strcpy(command, strtok(command, "\n"));


		// CASE 3: HISTORY
		// if the user input "!!", execute the most recent command
		if (strcmp(strtok(command, "\n"), "!!") == 0) {
			if (index == 0) {
				printf("No previous command exists\n");
			} else {
				strcpy(command, history[index-1]);
			}		
		}

		// CASE 3: HISTORY
		// if user inputs an index of the history of commands
		if ((atoi(command) != 0 && atoi(command) < index) || (strcmp(strtok(command, "\n"), "0") == 0)) {
			strcpy(command, history[atoi(command)]);
		}

		// CASE 3: HISTORY
		history[index] = malloc(sizeof(command)); // allocate memory
		strcpy(history[index], command); // copy command and store as history
		index += 1;


		// CASE 3: HISTORY
		// if input history index out of range
		if (atoi(command) > index) {
			printf("history index out of range\n");
		}

		// CASE 2: CHANGE DIRECTORY
		// if input is "cd" with no arguments, go to home directory
		else if (strcmp(command, "cd") == 0) {
			chdir(getenv("HOME"));
		}
		
		// CASE 2: CHANGE DIRECTORY
		// if first 2 characters of command == "cd"
		else if (strncmp("cd", command, 2) == 0) {  
			char *folder = strtok(command, " \n");  // store the rest of the command in a character array
			folder = strtok(NULL, " \n");
			valid = chdir(folder);  // check if directory is available
			if (valid == -1) {  // if directory is not available
				printf("Error: no such file or directory\n");
			}
		}

		// CASE 3: HISTORY
		// if command == "history", print all the previous commands from the array
		else if (strcmp(command, "history") == 0) {
			for (int i=0; i<index-1; i++) {
				printf("%d %s\n", i, history[i]);
			}
			index -= 1;
		}

		// CASE 1: CREATING EXTERNAL PROCESSES
		else {  // execute command
			if (strcmp(strtok(command, "\n"), "!!") != 0) {
				system(command);
			}
		}

	}
}
