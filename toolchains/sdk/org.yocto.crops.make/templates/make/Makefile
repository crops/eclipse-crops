.PHONY: default

default: all


.PHONY: all

all: hello

hello: hello.o
	$(CC) -o $@ $<
	
%.o: %.c
	$(CC) -c -o $@ $<

	
.PHONY: clean

clean:
	$(RM) *.o
	$(RM) hello