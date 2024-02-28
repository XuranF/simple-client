#https://www.cs.swarthmore.edu/~newhall/unixhelp/javamakefiles.html

JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $*.java

CLASSES = \
        client.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
