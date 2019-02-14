# This a Makefile, an input file for the 'make' program.  For you 
# command-line and Emacs enthusiasts, this makes it possible to build
# this program with a single command:
#     make 
# (or just 'make' if you are on a system that uses GNU make by default,
# such as Linux.) You can also clean up junk files and .class files with
#     make clean
# To run style61b (our style enforcer) over your source files, type
#     make style
# Finally, you can run tests with
#     make check

# This is not an especially efficient Makefile, because it's not easy to
# figure out the minimal set of Java files that need to be recompiled.  
# So if any .class file does not exist or is older than its .java file,
# we just remove all the .class files, compile the main class, and 
# then compile everything in the plugin directory.  

STYLEPROG = style61b

# All source files
SRCS := $(wildcard *.java)

# Flags to pass to Java compilations (include debugging info and report
# "unsafe" operations.)
JFLAGS = -g -Xlint:unchecked -Xlint:deprecation

CPATH = "..:$(CLASSPATH):;..;$(CLASSPATH)"

CLASSES = $(SRCS:.java=.class)

# A CLASSPATH value that (seems) to work on both Windows and Unix systems.
# To Unix, it looks like ..:$(CLASSPATH):JUNK and to Windows like
# JUNK;..;$(CLASSPATH).
CPATH = "..:$(CLASSPATH):;..;$(CLASSPATH)"

# Tell make that these are not really files.
.PHONY: clean default check unit style

# By default, make sure all classes are present and check if any sources have
# changed since the last build.
default: sentinel

# If any class is missing, or any source changed since the main classes were
# compiled, remove all class files and recompile.
sentinel: $(SRCS)	
	javac -cp $(CPATH) -sourcepath .. $(JFLAGS) $(SRCS)
	touch $@

# Run Tests.
check: unit

unit: sentinel
	java -ea -classpath $(CPATH) grader.SimpleUnitTest

# Check style of source files with style61b.
style:
	$(STYLEPROG) $(SRCS)

# Find and remove all *~, *.class, and testing output files.
# Do not touch .svn directories.
clean :
	$(RM) *~ *.class sentinel
