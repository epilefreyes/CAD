import os
import subprocess
import csv
import re
import multiprocessing
# Ruta al directorio de trabajo
project_dir = os.getcwd()  

# Función para cambiar al directorio de trabajo y compilar el proyecto con CMake
def compile_cmake():
    os.chdir(project_dir)
    
    # Crear la carpeta 'build' si no existe
    if not os.path.exists("build"):
        os.mkdir("build")
    
    os.chdir("build")
    
    # Ejecutar cmake y make
    subprocess.run(["cmake", ".."], check=True)
    subprocess.run(["make"], check=True)
    print("Proyecto compilado con éxito.")

# Función para ejecutar un programa con un número de threads y tamaño de matriz parametrizable
def run_program(program_name, threads, matrix_size):
    # Asegúrate de estar en la carpeta build
    os.chdir(os.path.join(project_dir, "build"))
    
    # Comando para ejecutar el programa
    command = f"./{program_name} {matrix_size} {threads}"
    
    try:
        # Ejecutar el programa y capturar la salida en tiempo real
        process = subprocess.Popen(command.split(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        final_number = None
        for line in process.stdout:
            print(line, end="")  # Imprimir cada línea como lo haría la consola
            match = re.search(r'\b\d+\b', line)
            if match:
                final_number = match.group(0)  # Capturar el número si aparece
        
        process.wait()  # Esperar a que el proceso termine completamente
        
        #print(f"Número final extraído: {final_number}")
        return final_number
    
    except subprocess.CalledProcessError as e:
        print(f"Error al ejecutar {program_name}: {e}")
        return None

# Función para escribir los resultados en un archivo CSV
def write_to_csv(program_name, threads, matrix_size, result, i):
    csv_file = os.path.join(project_dir, "resultados.csv")
    
    # Verificar si el archivo CSV ya existe
    file_exists = os.path.isfile(csv_file)
    
    # Escribir los resultados en el CSV
    with open(csv_file, mode='a', newline='') as file:
        writer = csv.writer(file)
        
        # Si el archivo no existe, escribir el encabezado
        if not file_exists:
            writer.writerow(["Programa", "Threads", "Tamaño Matriz", "Tiempo", "Iteracion"])
        
        # Escribir los datos
        writer.writerow([program_name, threads, matrix_size, result, i])

# Función principal para ejecutar todo el proceso
def main():
    # Compilar el proyecto
    compile_cmake()
    
    # Definir los parámetros
    program_name = ["matrizFxC", "matrizFxT", "mmPosix"]  # Puedes cambiar a "matrizFxT" o "mmPosix"
    # Solicitar al usuario el valor máximo de threads
    max_threads = int(multiprocessing.cpu_count())  # Obtiene el número máximo de threads según la máxima cantidad de núcleos del sistema
    # Comentario: Ahora el número máximo de threads se pasa como un argumento al ejecutar el script
    print(f"Número máximo de threads: {max_threads}")
    # Construir la lista de threads
    threads = [2**i for i in range(max_threads.bit_length()) if 2**i <= max_threads]
    
    # Asegurar que 1 esté siempre en la lista
    if 1 not in threads:
        threads.insert(0, 1)
    
    # Comentario: Esta implementación genera una lista de threads en potencias de 2
    # hasta el valor máximo ingresado por el usuario, incluyendo siempre el valor 1.
    matrix_size = [64, 128, 256, 512, 1024]  # Tamaño de la matriz en potencias de 2
    iteraciones = 30

    print(f"Lista de threads a utilizar: {threads}")
    print(f"Lista de tamaños de matriz: {matrix_size}")

    # Ejecutar el programa y guardar el resultado
    for i in range(iteraciones):
        print(f"Iteración {i+1} de {iteraciones}")
        for j in threads:
            for x in matrix_size:
                for z in program_name:
                    result = run_program(z, j, x)
                    write_to_csv(z, j, x, result, i+1)
if __name__ == "__main__":
    main()
