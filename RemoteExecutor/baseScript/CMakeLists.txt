cmake_minimum_required(VERSION 3.19)  # CMake version check
project(TallerCad)               


set(CMAKE_C_FLAGS "-fopenmp")
set(CMAKE_C_FLAGS_RELEASE "-O3")

# Add executable target with source files listed in SOURCE_FILES variable
add_executable(matrizFxC matrixFxC.c modulo.c)
add_executable(matrizFxT matrixFxT.c modulo.c)
add_executable(mmPosix MM_posix.c modulo.c)

if (CMAKE_C_COMPILER_ID STREQUAL "GNU" OR CMAKE_C_COMPILER_ID STREQUAL "Clang")
    target_compile_options(matrizFxT PRIVATE
        -Wall            # Enable all the most common warnings
        -Wextra          # Enable extra warnings
        -Wpedantic       # Enforce strict ISO C++ standards compliance
        -Wshadow         # Warn if a variable declaration shadows one from a parent scope
        -Wcast-align     # Warn about potential performance problem casts
        -Wconversion     # Warn for implicit type conversions that may change a value
        -Wduplicated-cond # Warn if 'if' and 'else' have identical conditions
        -Wnull-dereference # Warn if a null pointer dereference is detected

    )
    target_compile_options(matrizFxC PRIVATE
        -Wall            # Enable all the most common warnings
        -Wextra          # Enable extra warnings
        -Wpedantic       # Enforce strict ISO C++ standards compliance
        -Wshadow         # Warn if a variable declaration shadows one from a parent scope
        -Wcast-align     # Warn about potential performance problem casts
        -Wconversion     # Warn for implicit type conversions that may change a value
        -Wduplicated-cond # Warn if 'if' and 'else' have identical conditions
        -Wnull-dereference # Warn if a null pointer dereference is detected

    )
    target_compile_options(mmPosix PRIVATE
        -Wall            # Enable all the most common warnings
        -Wextra          # Enable extra warnings
        -Wpedantic       # Enforce strict ISO C++ standards compliance
        -Wshadow         # Warn if a variable declaration shadows one from a parent scope
        -Wcast-align     # Warn about potential performance problem casts
        -Wconversion     # Warn for implicit type conversions that may change a value
        -Wduplicated-cond # Warn if 'if' and 'else' have identical conditions
        -Wnull-dereference # Warn if a null pointer dereference is detected

    )

endif()
