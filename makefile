PROYECT_NAME = FreeBrowser
MAIN_CLASS = view.Main

CP = '.:assets/language:assets/lib/*:bin'

COPY_ASSETS = mkdir dist/assets; cp -r assets/language dist/assets/language; cp -r assets/html dist/assets/html; cp -r assets/lib dist/assets/lib

JAVAC = javac -source 1.5 -target 1.5 -classpath $(CP) -d bin
JAR = jar cvmf assets/Manifest.mf dist/$(PROYECT_NAME).jar -C bin .

.PHONY: main rclass rjar mclass mjar CLEAN CLASS_CLEAN JAR_CLEAN

main: mclass rclass

rclass: 
	java -cp $(CP) $(MAIN_CLASS) 

rjar: 
	cd dist;java -jar $(PROYECT_NAME).jar

mjar: dist JAR_CLEAN mclass
	$(JAR)
	$(COPY_ASSETS)

mclass: bin CLASS_CLEAN
	cp -r -t bin/ ./src/*
	find bin|grep '.java'|xargs rm
	find src|grep '.java'|xargs $(JAVAC) 

CLASS_CLEAN:
	rm -r bin/*; true

JAR_CLEAN:
	rm -r dist/*;true

CLEAN: CLASS_CLEAN JAR_CLEAN

bin:
	mkdir bin

dist:
	mkdir dist
