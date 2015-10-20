/*
**	blocked matrix multiplication example
**	compile with gcc -O3 -o mm-1 mm-1.c 
*/
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <strings.h>
#include <time.h>
#include <memory.h>

/* figures out number of elements in an array a */
#define    NELTS(a) (sizeof(a) / sizeof(a[0]))	
/* size of the matrices */
#define    N        1000
/* gives the min/max of a and b */
#define    min(a,b) ((a) < (b) ? (a) : (b))
#define    max(a,b) ((a) > (b) ? (a) : (b))

double    a[N][N];
double    b[N][N];
double    c[N][N];

void
mmulb(int b, double C[N][N], double A[N][N], double B[N][N])
{
    //for kk := 1 to N by B do
    //    for jj := 1 to N by B do
    //        for i := 1 to N do
    //            for k := kk to min(kk+B-1, N) do
    //                r = X[i,k]; [> register allocated <] 
    //                for j := jj to min(jj+B-1, N) do
    //                    Z[i,j] += r*Y[k,j];
    int i, j, jj, k, kk;
    double r;
    for (kk = 0; kk < N; kk += b) {
        for (jj = 0; jj < N; jj += b) {
            for (i = 0; i < N; i++) {
                for (k = kk; k < (kk+b < N ? kk+b : N); k++) {
                    r = A[i][k];
                    for (j = jj; j < (jj+b < N ? jj+b : N); j++) {
                        C[i][j] += r * B[k][j];
                    }
                }
            }
        } 
    }
}

void
mmul1(int b, double C[N][N], double A[N][N], double B[N][N])
{
    int    i, j, k;
    for (j = 0; j < N; j++) {
        for (k = 0; k < N; k++) {
            for (i = 0; i < N; i++) {
                C[j][i] += A[j][k] * B[k][i];
            }
        }
    }
}

/*
** standard matrix multiplication
*/
void
mmul0(int b, double C[N][N], double A[N][N], double B[N][N])
{
    int    i, j, k;

    for (i = 0; i < N; i++) {
        for (j = 0; j < N; j++) {
            for (k = 0; k < N; k++) {
                C[j][i] += A[j][k] * B[k][i];
            }
        }
    }
}

/*
** this is an array of pointers to the different 
** matrix multiplication functions
** (*mtab[i])() calls the ith function
** add your own routines and update this table
*/
void (*mtab[])(int b, double C[N][N], double A[N][N], double B[N][N]) =
    { mmul0, mmul1, mmulb };

/*
** choose the matrix multiplication routine to call
*/
void
mmul(int cmd, int bsize, double C[N][N], double A[N][N], double B[N][N])
{
    /* clear contents of C */
    memset((void *)C, '\0', sizeof(C[0][0]) * N * N);    
    (*mtab[cmd])(bsize, c, a, b);
}

/* fill a matrix with random numbers */
void
mfill(double A[N][N])
{
    int i, j;

    for (i = 0; i < N; i++)
        for (j = 0; j < N; j++)
            A[i][j] = (double )rand()/(RAND_MAX+1.0) - 0.5;
}

/* sum all entries of the matrix */
double
msum(double A[N][N])
{
    int    i, j;
    double    sum = 0;

    for (i = 0; i < N; i++)
        for (j = 0; j < N; j++)
            sum += A[i][j];
    return sum;
}

void
runmm(int bsize)
{
    clock_t   start;
    double    elapsed;
    int       i;

    srand(1);    /* initialise seed so we get same result every run */
    mfill(a);
    mfill(b);
    printf("Block size = %d, N = %d\n", bsize, N);
    for (i = 0; i < NELTS(mtab); i++) {
        start = clock();
        mmul(i, bsize, c, b, a);
        elapsed = (double)(clock() - start) / CLOCKS_PER_SEC;
        printf("--- fn = mmul%d, ", i);
        printf("MFLOPS = %g, elapsed time = %gs, sum = %g\n", 
            2.0 * N * N * N / 1.0e6 / elapsed, elapsed, msum(c));
    }
}

usage(char *myname)
{
    (void)fprintf(stderr, "%s [-b bsize]\n", myname);
    exit(1);
}

int
main(int argc, char *argv[])
{
    char *myname = argv[0];
    int ch;
    int vflag = 0, bsize = 50;

    while ((ch = getopt(argc, argv, "b:")) != -1) {
        switch (ch) {
        case 'v':
            vflag = 1;
            break;
        case 'b':
            bsize = atoi(optarg);
            if (bsize < 0) { 
                usage(myname);
            }
            break;
        default:
            usage(myname);
        }
    }
    argc -= optind;
    argv += optind;
    runmm(bsize);
    return 0;
}
