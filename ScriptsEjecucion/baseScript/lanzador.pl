#!/usr/bin/perl
#**************************************************************
#         		Pontificia Universidad Javeriana
#     Autor: Felipe Reyes Palacio, basado en script de J. Corredor
#     Fecha: Octubre de 2024
#     Materia: Computacion de Alto Desempeño
#     Tema: Taller de Evaluacion de Rendimiento
#     Fichero: Script automatizacion para ejecucion masiva de prueba de rendimento
#****************************************************************/

use strict;
use warnings;
use File::Basename;
use File::Path qw(make_path);
use IPC::Open3;
use Symbol 'gensym';

# Ruta al directorio de trabajo
my $project_dir = `pwd`;
chomp($project_dir);

# Crear la carpeta 'build' si no existe
unless (-d "build") {
	make_path("build");
}

# Crear la carpeta 'build' si no existe
unless (-d "salidas") {
	make_path("salidas");
}

# Funcion para cambiar al directorio de trabajo y compilar el proyecto con CMake
sub compile_cmake {
    chdir($project_dir) or die "No se puede cambiar al directorio: $!";
    
    chdir("build") or die "No se puede cambiar al directorio: $!";
    
    # Ejecutar cmake y make
    system("cmake ..") == 0 or die "Error al ejecutar cmake: $!";
    system("make") == 0 or die "Error al ejecutar make: $!";
    print "Proyecto compilado con axito.\n";
}

# Funcion para ejecutar lshw
sub run_lshw_system {
    
    my $hardware_file = "$project_dir/salidas/hardware.txt";
    my $os_output = "$project_dir/salidas";
    # Comando para ejecutar el programa
    my $command = "lshw >$hardware_file";
    
    my $stderr = gensym;
    my $pid = open3(undef, \*CHLD_OUT, $stderr, $command);
    waitpid($pid, 0);  # Esperar a que el proceso termine completamente
	
    my $command = "cp /etc/os-release $os_output";
    
    my $stderr = gensym;
    my $pid = open3(undef, \*CHLD_OUT, $stderr, $command);
    waitpid($pid, 0);  # Esperar a que el proceso termine completamente

}

# Funcion para ejecutar un programa con un numero de threads y tamaño de matriz parametrizable
sub run_program {
    my ($program_name, $threads, $matrix_size) = @_;
    
    # Asegurate de estar en la carpeta build
    chdir("$project_dir/build") or die "No se puede cambiar al directorio: $!";
    
    # Comando para ejecutar el programa
    my $command = "./$program_name $matrix_size $threads";
    
    my $final_number;
    my $stderr = gensym;
    my $pid = open3(undef, \*CHLD_OUT, $stderr, $command);
    
    while (my $line = <CHLD_OUT>) {
        if ($line =~ /\b(\d+)\b/) {
            $final_number = $1;  # Capturar el numero si aparece
        }
    }
    
    waitpid($pid, 0);  # Esperar a que el proceso termine completamente
    
    return $final_number;
}

# Funcion para escribir los resultados en un archivo CSV
sub write_to_csv {
    my ($program_name, $threads, $matrix_size, $result, $i) = @_;

    my $csv_file = "$project_dir/salidas/resultados.csv";
    # Verificar si el archivo CSV ya existe
    my $file_exists = -e $csv_file;
    
    # Escribir los resultados en el CSV
    open(FH, '>>', $csv_file) or die $!;
    
    # Si el archivo no existe, escribir el encabezado
    unless ($file_exists) {
		print FH "Programa,Threads,Tamano,Tiempo,Iteracion\n";
    }
    
    # Escribir los datos
	print FH $program_name, ",", $threads,",", $matrix_size,",", $result,",", $i,"\n";
    close(FH);
}

# Funcion principal para ejecutar todo el proceso
sub main {

    # Compilar el proyecto
    compile_cmake();
    
    # Definir los parametros
    my $csv_file = "$project_dir/salidas/resultados.csv";
	unlink $csv_file;
	
    my @program_name = ("matrizFxC", "matrizFxT", "mmPosix");
    my $max_threads = `nproc`;
    chomp($max_threads);
    
    print "Generando listado de hardware y software...\n";
	run_lshw_system();
	
    print "Numero maximo de threads: $max_threads\n";
    
    # Construir la lista de threads
    my @threads = map { 2**$_ } grep { 2**$_ <= $max_threads } (0..int(log($max_threads)/log(2)));
    
    # Asegurar que 1 esta siempre en la lista
    unshift(@threads, 1) unless grep { $_ == 1 } @threads;
    
    my @matrix_size = (64, 128, 256, 512, 1024);
    my $iteraciones = 30;

    print "Lista de threads a utilizar: @threads\n";
    print "Lista tamanos de matriz: @matrix_size\n";

    # Ejecutar el programa y guardar el resultado
    for my $i (0..$iteraciones-1) {
		my $current_date = `date  --rfc-3339=seconds`;
		chomp($current_date);
		
        print $current_date, "\tIteracion ", $i + 1, " de $iteraciones\n";
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

