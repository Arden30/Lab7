# Laboratory work 5
Реализовать консольное приложение, которое реализует управление коллекцией объектов в интерактивном режиме. В коллекции необходимо хранить объекты класса LabWork, описание которого приведено ниже.
Разработанная программа должна удовлетворять следующим требованиям:
•	Класс, коллекцией экземпляров которого управляет программа, должен реализовывать сортировку по умолчанию.
•	Все требования к полям класса (указанные в виде комментариев) должны быть выполнены.
•	Для хранения необходимо использовать коллекцию типа java.util.ArrayDequeue
•	При запуске приложения коллекция должна автоматически заполняться значениями из файла.
•	Имя файла должно передаваться программе с помощью: аргумент командной строки.
•	Данные должны храниться в файле в формате xml
•	Чтение данных из файла необходимо реализовать с помощью класса java.io.InputStreamReader
•	Запись данных в файл необходимо реализовать с помощью класса java.io.FileOutputStream
•	Все классы в программе должны быть задокументированы в формате javadoc.
•	Программа должна корректно работать с неправильными данными (ошибки пользовательского ввода, отсутсвие прав доступа к файлу и т.п.).
В интерактивном режиме программа должна поддерживать выполнение следующих команд:
•	help : вывести справку по доступным командам
•	info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)
•	show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении
•	add {element} : добавить новый элемент в коллекцию
•	update id {element} : обновить значение элемента коллекции, id которого равен заданному
•	remove_by_id id : удалить элемент из коллекции по его id
•	clear : очистить коллекцию
•	save : сохранить коллекцию в файл
•	execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.
•	exit : завершить программу (без сохранения в файл)
•	head : вывести первый элемент коллекции
•	add_if_min {element} : добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции
•	remove_lower {element} : удалить из коллекции все элементы, меньшие, чем заданный
•	filter_less_than_maximum_point maximumPoint : вывести элементы, значение поля maximumPoint которых меньше заданного
•	filter_greater_than_difficulty difficulty : вывести элементы, значение поля difficulty которых больше заданного
•	print_field_descending_discipline : вывести значения поля discipline всех элементов в порядке убывания
Формат ввода команд:
•	Все аргументы команды, являющиеся стандартными типами данных (примитивные типы, классы-оболочки, String, классы для хранения дат), должны вводиться в той же строке, что и имя команды.
•	Все составные типы данных (объекты классов, хранящиеся в коллекции) должны вводиться по одному полю в строку.
•	При вводе составных типов данных пользователю должно показываться приглашение к вводу, содержащее имя поля (например, "Введите дату рождения:")
•	Если поле является enum'ом, то вводится имя одной из его констант (при этом список констант должен быть предварительно выведен).
•	При некорректном пользовательском вводе (введена строка, не являющаяся именем константы в enum'е; введена строка вместо числа; введённое число не входит в указанные границы и т.п.) должно быть показано сообщение об ошибке и предложено повторить ввод поля.
•	Для ввода значений null использовать пустую строку.
•	Поля с комментарием "Значение этого поля должно генерироваться автоматически" не должны вводиться пользователем вручную при добавлении.
Описание хранимых в коллекции классов:
public class LabWork {
    private Long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private java.time.LocalDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Long minimalPoint; //Поле может быть null, Значение поля должно быть больше 0
    private Integer maximumPoint; //Поле может быть null, Значение поля должно быть больше 0
    private Long personalQualitiesMinimum; //Поле может быть null, Значение поля должно быть больше 0
    private Difficulty difficulty; //Поле может быть null
    private Discipline discipline; //Поле не может быть null
}
public class Coordinates {
    private long x; //Максимальное значение поля: 54
    private Double y; //Максимальное значение поля: 101, Поле не может быть null
}
public class Discipline {
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Long lectureHours; //Поле не может быть null
    private Long practiceHours; //Поле не может быть null
}
public enum Difficulty {
    EASY,
    VERY_HARD,
    HOPELESS,
    TERRIBLE;
}
