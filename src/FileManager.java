import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;

public class FileManager {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            String currentDirectory = promptForDirectory(scanner);
            handleUserCommands(scanner, currentDirectory);
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static String promptForDirectory(Scanner scanner) {
        while (true) {
            System.out.print("Введите директорию: ");
            String directoryPath = scanner.nextLine();
            if (Files.isDirectory(Paths.get(directoryPath))) {
                return directoryPath;
            } else {
                System.out.println("Некорректный ввод директории. Попробуйте снова.");
            }
        }
    }

    private static void handleUserCommands(Scanner scanner, String currentDirectory) {
        while (true) {
            System.out.println("Текущая директория: " + currentDirectory);
            System.out.print("Введите команду (ls, cp, cd, exit): ");
            String command = scanner.nextLine();
            String[] commandParts = command.trim().split(" ");
            String action = commandParts[0];

            switch (action) {
                case "ls":
                    listFiles(currentDirectory);
                    break;
                case "cp":
                    handleFileCopy(scanner, currentDirectory);
                    break;
                case "cd":
                    currentDirectory = changeDirectory(scanner, currentDirectory);
                    break;
                case "exit":
                    System.out.println("Выход из файлового менеджера.");
                    return;
                default:
                    System.out.println("Неизвестная команда. Попробуйте снова.");
                    break;
            }
        }
    }

    private static String changeDirectory(Scanner scanner, String currentDirectory) {
        System.out.print("Введите новую директорию: ");
        String newDirectory = scanner.nextLine();
        if (Files.isDirectory(Paths.get(newDirectory))) {
            return newDirectory;
        } else {
            System.out.println("Некорректный ввод директории: " + newDirectory);
            return currentDirectory; // возвращаем старую директорию
        }
    }

    private static void handleFileCopy(Scanner scanner, String currentDirectory) {
        try {
            String sourcePath = promptForPath(scanner, "Введите полный путь к файлу-источнику:");
            String destinationPath = promptForPath(scanner, "Введите полный путь к файлу-назначению:");
            if (copyFileToDirectory(sourcePath, destinationPath, currentDirectory)) {
                System.out.println("Файл успешно скопирован в папку назначения.");
            }
        } catch (IOException e) {
            System.out.println("Некорректный ввод файла-источника: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Некорректный ввод: " + e.getMessage());
        }
    }

    private static String promptForPath(Scanner scanner, String message) {
        System.out.println(message);
        return scanner.nextLine();
    }

    private static void listFiles(String directoryPath) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
            System.out.println("Содержимое директории:");
            for (Path entry : directoryStream) {
                displayFileInfo(entry);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при перечислении файлов: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Некорректный ввод: " + e.getMessage());
        }
    }

    private static void displayFileInfo(Path filePath) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            if (attrs.isDirectory()) {
                long directorySize = calculateDirectorySize(filePath);
                System.out.format("Папка: %s, Размер: %d байт%n", filePath.getFileName(), directorySize);
            } else {
                System.out.format("Файл: %s, Размер: %d байт%n", filePath.getFileName(), attrs.size());
            }
        } catch (IOException e) {
            System.out.println("Не удалось получить информацию о файле: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Некорректный ввод: " + e.getMessage());
        }
    }

    private static long calculateDirectorySize(Path directory) throws IOException {
        final long[] size = {0};
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                size[0] += attrs.size();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.out.println("Не удалось получить доступ к файлу: " + file + " " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return size[0];
    }

    private static boolean copyFileToDirectory(String source, String destination, String currentDirectory) throws IOException {
        Path sourcePath = Paths.get(source);
        Path destinationDirPath = Paths.get(destination);

        if (!Files.exists(sourcePath)) {
            throw new IOException("Файл-источник не существует: " + sourcePath);
        }

        if (!Files.exists(destinationDirPath)) {
            // Создаем папку назначения в текущей директории
            destinationDirPath = Paths.get(currentDirectory, destinationDirPath.toString());
            Files.createDirectories(destinationDirPath);
        }

        Path destinationPath = destinationDirPath.resolve(sourcePath.getFileName());

        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Ошибка при копировании файла: " + e.getMessage());
            return false;
        }

        return true;
    }
}