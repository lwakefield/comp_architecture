mm-1:	mm-1.o
	g++ -msse2 -O3 -I ./eigen -o mm-1 mm-1.cpp

mm-blas:	mm-blas.o
	gcc -O3 -o mm-blas mm-blas.c -lblas

clean:
	-rm -f *.o mm-1 mm-blas
