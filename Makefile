.PHONY: build clean zip run

build:
	touch testfile.txt input.txt
	cd ./auto/ && $(MAKE) 
	
clean:
	cd ./auto/ && $(MAKE) clean
	rm *.txt *.zip
	
zip:
	./auto/zip.sh

run:
	cd ./auto/ && $(MAKE) run

ir:
	cd ./auto && $(MAKE) ir

mips:
	cd ./auto && $(MAKE) mips