/*#######################################################################################
* Fecha: 20 sep 2024
* Autor: J. Corredor, PhD.
* Tema: Programación Modular en C
*  - Programa Multiplicación de Matrices algoritmo clásico
*  - Creación de funciones: Modularidad
*  - Creación de interfaz
*  - Compilación: automatización
*  - Paralelismo con Posix
*  - Paralelismo con OpenMP
######################################################################################*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <sys/time.h>
#include <omp.h>
#include "modulo.h" 


int main(int argc, char *argv[]){
    int N = atoi(argv[1]);
    int HILOS = atoi(argv[2]);
    size_t *matrixA  = (size_t *)calloc(N*N, sizeof(size_t));
    size_t *matrixB  = (size_t *)calloc(N*N, sizeof(size_t));
    size_t *matrixC  = (size_t *)calloc(N*N, sizeof(size_t));
    srand(time(NULL));


    omp_set_num_threads(HILOS);
    iniMatrix(matrixA, matrixB, N);

    impMatrix(matrixA, N, 0);
    impMatrix(matrixB, N, 1);

    InicioMuestra();
    multiMatrixTranspuesta(matrixA, matrixB, matrixC, N);
    FinMuestra();
    Muestra();

    impMatrix(matrixC, N, 0);

    /*Liberación de Memoria*/
    free(matrixA);
    free(matrixB);
    free(matrixC);
   
    return 0;

}

 
