package edu.javeriana.cad.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.javeriana.cad.CADExecuteFull;

public class MainExecuteFullTest {

	public static void main(String[] args) {
		if (args == null || args.length!=3) {
			printHelp();
			System.exit(1);
			return;
		}

		Path outputPath = Paths.get(args[0]);
		if (!Files.exists(outputPath)) {
			System.err.println("Output path doesn't exists:" + outputPath.toString());
			System.exit(1);
			return;
		}
		
		String instanceType = args[1];

		boolean isx86 = args[2].equalsIgnoreCase("true");
		
		try {
			CADExecuteFull.executeFullTest(outputPath, instanceType, isx86);
			System.out.println("Finalizado correctamente!!!!!");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private static void printHelp() {
		System.err.println("Call it with output path, machine type and if isx86 (or not)");
		
	}

}
