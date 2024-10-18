#!/usr/bin/perl
#**************************************************************
#         		Pontificia Universidad Javeriana
#     Autor: Felipe Reyes Palacio, basado en script de J. Corredor
#     Fecha: Octubre de 2024
#     Materia: Computación de Alto Desempeño
#     Tema: Taller de Evaluación de Rendimiento
#     Fichero: Script automatización para ejecución masiva de prueba de rendimento
#****************************************************************/

use strict;
use warnings;
use File::Basename;
use File::Path qw(make_path);
use IPC::Open3;
use Symbol 'gensym';
use Text::CSV;

# Ruta al directorio de trabajo
my $project_dir = `pwd`;
chomp($project_dir);

# Función para cambiar al directorio de trabajo y compilar el proyecto con CMake
sub compile_cmake {
    chdir($project_dir) or die "No se puede cambiar al directorio: $!";
    
    # Crear la carpeta 'build' si no existe
    unless (-d "build") {
        make_path("build");
    }
    
    chdir("build") or die "No se puede cambiar al directorio: $!";
    
    # Ejecutar cmake y make
    system("cmake ..") == 0 or die "Error al ejecutar cmake: $!";
    system("make") == 0 or die "Error al ejecutar make: $!";
    print "Proyecto compilado con éxito.\n";
}

# Función para ejecutar un programa con un número de threads y tamaño de matriz parametrizable
sub run_program {
    my ($program_name, $threads, $matrix_size) = @_;
    
    # Asegúrate de estar en la carpeta build
    chdir("$project_dir/build") or die "No se puede cambiar al directorio: $!";
    
    # Comando para ejecutar el programa
    my $command = "./$program_name $matrix_size $threads";
    
    my $final_number;
    my $stderr = gensym;
    my $pid = open3(undef, \*CHLD_OUT, $stderr, $command);
    
    while (my $line = <CHLD_OUT>) {
        if ($line =~ /\b(\d+)\b/) {
            $final_number = $1;  # Capturar el número si aparece
        }
    }
    
    waitpid($pid, 0);  # Esperar a que el proceso termine completamente
    
    return $final_number;
}

# Función para escribir los resultados en un archivo CSV
sub write_to_csv {
    my ($program_name, $threads, $matrix_size, $result, $i) = @_;
    my $csv_file = "$project_dir/resultados.csv";
    
    # Verificar si el archivo CSV ya existe
    my $file_exists = -e $csv_file;
    
    # Escribir los resultados en el CSV
    open my $file, '>>', $csv_file or die "No se puede abrir el archivo: $!";
    my $csv = Text::CSV->new({ binary => 1, auto_diag => 1 });
    
    # Si el archivo no existe, escribir el encabezado
    unless ($file_exists) {
        $csv->print($file, ["Programa", "Threads", "Tamaño Matriz", "Tiempo", "Iteracion"]);
    }
    
    # Escribir los datos
    $csv->print($file, [$program_name, $threads, $matrix_size, $result, $i]);
    close $file;
}

# Función principal para ejecutar todo el proceso
sub main {

    # Compilar el proyecto
    compile_cmake();
    
    # Definir los parámetros
    my @program_name = ("matrizFxC", "matrizFxT", "mmPosix");
    my $max_threads = `nproc`;
    chomp($max_threads);
    
    print "Número máximo de threads: $max_threads\n";
    
    # Construir la lista de threads
    my @threads = map { 2**$_ } grep { 2**$_ <= $max_threads } (0..int(log($max_threads)/log(2)));
    
    # Asegurar que 1 esté siempre en la lista
    unshift(@threads, 1) unless grep { $_ == 1 } @threads;
    
    my @matrix_size = (64, 128, 256, 512, 1024);
    my $iteraciones = 30;

    print "Lista de threads a utilizar: @threads\n";
    print "Lista de tamaños de matriz: @matrix_size\n";

    # Ejecutar el programa y guardar el resultado
    for my $i (0..$iteraciones-1) {
        print "Iteración ", $i + 1, " de $iteraciones\n";
        for my $j (@threads) {
            for my $x (@matrix_size) {
                for my $z (@program_name) {
                    my $result = run_program($z, $j, $x);
                    write_to_csv($z, $j, $x, $result, $i + 1);
                }
            }
        }
    }
}

main();

