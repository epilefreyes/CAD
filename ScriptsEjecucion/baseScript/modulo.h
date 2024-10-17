#ifndef MODULO_H
#define MODULO_H


#include <pthread.h>

#define DATA_SIZE (1024*1024*64*3)
extern double *mA, *mB, *mC;
extern pthread_mutex_t MM_mutex;
extern double MEM_CHUNK[DATA_SIZE];

/**
 * @brief Inicializa la medición de tiempo.
 */
void InicioMuestra();

/**
 * @brief Finaliza la medición de tiempo.
 */
void FinMuestra();

/**
 * @brief Muestra el tiempo transcurrido.
 */
void Muestra();

/**
 * @brief Imprime la matriz según el formato especificado.
 * 
 * @param matrix Puntero a los datos de la matriz.
 * @param D Dimensión de la matriz.
 * @param t Tipo de formato de impresión (0: estándar, 1: transpuesta).
 */
void impMatrix(size_t *matrix, int D, int t);


/**
 * @brief Inicializa dos matrices con valores aleatorios.
 * 
 * @param m1 Puntero a la primera matriz.
 * @param m2 Puntero a la segunda matriz.
 * @param D Dimensión de las matrices.
 */
void iniMatrix(size_t *m1, size_t *m2, int D);


/**
 * @brief Multiplica dos matrices usando el algoritmo clásico (fila x columna).
 * 
 * @param mA Puntero a la primera matriz.
 * @param mB Puntero a la segunda matriz.
 * @param mC Puntero a la matriz resultado.
 * @param D Dimensión de las matrices.
 */
void multiMatrixClasica(size_t *mA, size_t *mB, size_t *mC, int D);

/**
 * @brief Multiplica dos matrices usando el algoritmo transpuesto.
 * 
 * @param mA Puntero a la primera matriz.
 * @param mB Puntero a la segunda matriz.
 * @param mC Puntero a la matriz resultado.
 * @param D Dimensión de las matrices.
 */
void multiMatrixTranspuesta(size_t *mA, size_t *mB, size_t *mC, int D);
// Define struct parametros
// Definir la estructura parametros
/**
 * @brief Estructura para los parámetros del hilo.
 */
struct parametros {
    int nH; ///< Número de hilos
    int idH; ///< ID del hilo
    int N; ///< Tamaño de las matrices
};

/**
 * @brief Imprime una matriz de tamaño específico.
 * 
 * @param sz Tamaño de la matriz.
 * @param matriz Puntero a los datos de la matriz.
 */
void print_matrix(int sz, double *matriz); 

/**
 * @brief Llena una matriz con valores específicos según su tamaño.
 * 
 * @param SZ Tamaño de la matriz.
 */
void llenar_matriz(int SZ);



/**
 * @brief Función ejecutada por cada hilo para realizar la multiplicación de matrices.
 * 
 * @param variables Puntero a la estructura que contiene los parámetros del hilo.
 * @return Puntero al resultado (NULL).
 */
void *mult_thread(void *variables);
#endif
