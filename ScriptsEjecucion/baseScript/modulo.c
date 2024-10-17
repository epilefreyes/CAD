/*#######################################################################################
 * Fecha: 20 sep 2024
 * Autor: J. Corredor, PhD
 * Tema: Programación Modular en C
 * 	- Programa Multiplicación de Matrices algoritmo clásico
 * 	- Creación de funciones: Modularidad
 * 	- Creación de interfaz
 * 	- Compilación: automatización
 * 	- Paralelismo con Posix
 * 	- Paralelismo con OpenMP
######################################################################################*/
#include <pthread.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <sys/time.h>
#include <omp.h>

#include "modulo.h"

struct timeval ini; ///< Tiempo de inicio para las mediciones
struct timeval fin; ///< Tiempo de finalización para las mediciones


void InicioMuestra(){
	gettimeofday(&ini, (void *)0);
}

void FinMuestra(){
gettimeofday(&fin, (void *)0);
}

void Muestra(){
	fin.tv_usec -= ini.tv_usec;
	fin.tv_sec  -= ini.tv_sec;
	double tiempo = (double) (fin.tv_sec*1000000 + fin.tv_usec); 
	printf("%9.0f \n", tiempo);
}

	/**Función Impresión de Matriz**/	
/**
 * @brief Imprime la matriz según el formato de impresión especificado.
 * 
 * @param matrix Puntero a los datos de la matriz.
 * @param D Dimensión de la matriz.
 * @param t Tipo de impresión.
 */
void impMatrix(size_t *matrix, int D, int t){
	int aux = 0;
	if(D < 6)
		switch(t){
			case 0:
				for(int i=0; i<D*D; i++){
					if(i%D==0) printf("\n");
						printf("%zu ", matrix[i]);
				}
				printf("\n - \n");
				break;
			case 1:
				while(aux<D){
					for(int i=aux; i<D*D; i+=D)
							printf("%zu ", matrix[i]);
					aux++;
					printf("\n");
				}
				break;
			default:
				printf("Sin tipo de impresión \n");
		}
}

	/**INICIALIZACIÖN de Matriz**/
/**
 * @brief Inicializa dos matrices con valores aleatorios.
 * 
 * @param m1 Puntero a la primera matriz.
 * @param m2 Puntero a la segunda matriz.
 * @param D Dimensión de las matrices.
 */
void iniMatrix(size_t *m1, size_t *m2, int D){
	for(int i=0; i<D*D; i++, m1++, m2++){
		*m1 = (size_t) rand()%10;	
		*m2 = (size_t) rand()%10;	
	}
}

	//Multiplicación de Matrices mAxmB=mC (algoritmo clásico FilasxColumnas)
/**
 * @brief Multiplica dos matrices usando el algoritmo clásico (fila x columna).
 * 
 * @param mA Puntero a la primera matriz.
 * @param mB Puntero a la segunda matriz.
 * @param mC Puntero a la matriz resultado.
 * @param D Dimensión de las matrices.
 */
void multiMatrixClasica(size_t *mA, size_t *mB, size_t *mC, int D){
	

#pragma omp parallel
{
size_t Suma, *pA, *pB;
	int threads; 
	threads = omp_get_num_threads();

    #pragma omp for
	for(int i=0; i<D; i++){
		for(int j=0; j<D; j++){
			pA = mA+i*D;	
			pB = mB+j; 
			Suma = 0.0;
			for(int k=0; k<D; k++, pA++, pB+=D){
				Suma += *pA * *pB;
			}
			mC[i*D+j] = Suma;
		}
	}
  }
}

	//Multiplicación de Matrices mAxmB=mC (algoritmo Transpuesta) 

/**
 * @brief Multiplica dos matrices usando el algoritmo transpuesto.
 * 
 * @param mA Puntero a la primera matriz.
 * @param mB Puntero a la segunda matriz.
 * @param mC Puntero a la matriz resultado.
 * @param D Dimensión de las matrices.
 */
void multiMatrixTranspuesta(size_t *mA, size_t *mB, size_t *mC, int D){
	
  #pragma omp parallel
  {
    size_t Suma, *pA, *pB;
    #pragma omp for
	for(int i=0; i<D; i++){
		for(int j=0; j<D; j++){
			pA = mA+i*D;	
			pB = mB+j*D;	
			Suma = 0.0;
			for(int k=0; k<D; k++, pA++, pB++){
				Suma += *pA * *pB;
			}
			mC[i*D+j] = Suma;
		}
	}

  }
}


#define DATA_SIZE (1024*1024*64*3)

pthread_mutex_t MM_mutex; ///< Mutex para la sincronización
double MEM_CHUNK[DATA_SIZE]; ///< Bloque de memoria para el almacenamiento de datos
double *mA, *mB, *mC; ///< Punteros globales a las matrices
/**
 * @brief Imprime la matriz de tamaño específico.
 * 
 * @param sz Tamaño de la matriz.
 * @param matriz Puntero a los datos de la matriz.
 */
void print_matrix(int sz, double *matriz) {
    if (sz < 12) {
        for (int i = 0; i < sz * sz; i++) {
            if (i % sz == 0) printf("\n");
            printf(" %.3f ", matriz[i]);
        }
    }
}

/**
 * @brief Llena la matriz con valores específicos.
 * 
 * @param SZ Tamaño de la matriz.
 */
void llenar_matriz(int SZ){
    srand48(time(NULL));
    for (int i = 0; i < SZ * SZ; i++){
        mA[i] = 1.1 * i;
        mB[i] = 2.2 * i;
        mC[i] = 0;
    }
}
/**
 * @brief Función ejecutada por cada hilo para realizar la multiplicación de matrices.
 * 
 * @param variables Puntero a la estructura que contiene los parámetros del hilo.
 * @return Puntero al resultado (NULL).
 */
void *mult_thread(void *variables){
	struct parametros *data = (struct parametros *)variables;
	
	int idH = data->idH;
	int nH  = data->nH;
	int N   = data->N;
	int ini = (N/nH)*idH;
	int fin = (N/nH)*(idH+1);

    for (int i = ini; i < fin; i++){
        for (int j = 0; j < N; j++){
			double *pA, *pB, sumaTemp = 0.0;
			pA = mA + (i*N); 
			pB = mB + j;
            for (int k = 0; k < N; k++, pA++, pB+=N){
				sumaTemp += (*pA * *pB);
			}
			mC[i*N+j] = sumaTemp;
		}
	}

	pthread_mutex_lock (&MM_mutex);
	pthread_mutex_unlock (&MM_mutex);
	pthread_exit(NULL);
}


